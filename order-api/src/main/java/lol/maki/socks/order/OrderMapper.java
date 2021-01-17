package lol.maki.socks.order;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Repository
public class OrderMapper {
	private final JdbcTemplate jdbcTemplate;

	private final ResultSetExtractor<List<Order>> ordersResultSetExtractor = rs -> {
		final List<Order> orders = new ArrayList<>();
		Order lastOrder = null; ;
		String lastOrderId = null;
		while (rs.next()) {
			final String orderId = rs.getString("order_id");
			if (lastOrder == null || !Objects.equals(lastOrderId, orderId)) {
				if (lastOrder != null) {
					orders.add(lastOrder);
				}
				lastOrder = new Order(
						orderId,
						new Customer(
								rs.getString("customer_id"),
								rs.getString("customer_first_name"),
								rs.getString("customer_last_name"),
								rs.getString("customer_username")),
						new Address(
								rs.getString("address_number"),
								rs.getString("address_street"),
								rs.getString("address_city"),
								rs.getString("address_postcode"),
								rs.getString("address_country")),
						new Card(
								rs.getString("card_long_num"),
								rs.getDate("card_expires").toLocalDate(),
								rs.getString("card_ccv")),
						new ArrayList<>(),
						new Shipment(
								rs.getString("shipment_carrier"),
								UUID.fromString(rs.getString("shipment_tracking_number")),
								rs.getDate("shipment_delivery_date").toLocalDate()),
						rs.getTimestamp("date").toInstant().atOffset(OffsetDateTime.now().getOffset()),
						OrderStatus.fromValue(rs.getInt("status")));
			}
			lastOrder.addItem(new Item(
					orderId,
					rs.getString("item_id"),
					rs.getInt("quantity"),
					rs.getBigDecimal("unit_price")));
			lastOrderId = orderId;
		}
		if (lastOrder != null) {
			orders.add(lastOrder);
		}
		return orders;
	};

	public OrderMapper(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Optional<Order> findOne(String orderId) {
		final List<Order> orders = this.jdbcTemplate.query("SELECT o.order_id, o.customer_id, o.customer_first_name, o.customer_last_name, o.customer_username, o.address_number, o.address_street, o.address_city, o.address_postcode, o.address_country, o.card_long_num, o.card_expires, o.card_ccv, o.shipment_carrier, o.shipment_tracking_number, o.shipment_delivery_date, o.date, MAX(s.status) AS status, i.item_id, i.quantity, i.unit_price  FROM `order` AS o INNER JOIN order_status AS s ON o.order_id = s.order_id INNER JOIN order_item AS i ON o.order_id = i.order_id GROUP BY order_id, item_id HAVING order_id = ?", this.ordersResultSetExtractor, orderId);
		if (CollectionUtils.isEmpty(orders)) {
			return Optional.empty();
		}
		else {
			return Optional.ofNullable(orders.get(0));
		}
	}

	public List<Order> findByCustomerId(String customerId) {
		return this.jdbcTemplate.query("SELECT o.order_id, o.customer_id, o.customer_first_name, o.customer_last_name, o.customer_username, o.address_number, o.address_street, o.address_city, o.address_postcode, o.address_country, o.card_long_num, o.card_expires, o.card_ccv, o.shipment_carrier, o.shipment_tracking_number, o.shipment_delivery_date, o.date, MAX(s.status) AS status, i.item_id, i.quantity, i.unit_price  FROM `order` AS o INNER JOIN order_status AS s ON o.order_id = s.order_id INNER JOIN order_item AS i ON o.order_id = i.order_id GROUP BY order_id, item_id, customer_id, date HAVING customer_id = ? ORDER BY date DESC", this.ordersResultSetExtractor, customerId);
	}

	@Transactional
	public int[] insert(Order order) {
		final Customer customer = order.customer();
		final Address address = order.address();
		final Card card = order.card();
		final Shipment shipment = order.shipment();
		final int orderUpdated = this.jdbcTemplate.update("INSERT INTO `order`(order_id, customer_id, customer_first_name, customer_last_name, customer_username, address_number, address_street, address_city, address_postcode, address_country, card_long_num, card_expires, card_ccv, shipment_carrier, shipment_tracking_number, shipment_delivery_date, date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				order.id(),
				customer.id(),
				customer.firstName(),
				customer.lastName(),
				customer.username(),
				address.number(),
				address.street(),
				address.city(),
				address.postcode(),
				address.country(),
				card.longNum(),
				card.expires(),
				card.ccv(),
				shipment.carrier(),
				shipment.trackingNumber().toString(),
				shipment.deliveryDate(),
				order.date().toLocalDateTime());
		final int statusUpdated = this.jdbcTemplate.update("INSERT INTO order_status(order_id, status, updated_at) VALUES (?, ?, ?)",
				order.id(),
				order.status().value(),
				LocalDateTime.now());
		final int[] itemBatchUpdated = this.jdbcTemplate.batchUpdate("INSERT INTO order_item(order_id, item_id, quantity, unit_price) VALUES (?, ?, ?, ?)",
				order.items()
						.stream()
						.map(item -> new Object[] { order.id(), item.itemId(), item.quantity(), item.unitPrice() })
						.collect(Collectors.toUnmodifiableList()));
		return new int[] { orderUpdated, statusUpdated, IntStream.of(itemBatchUpdated).sum() };
	}
}
