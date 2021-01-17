package lol.maki.socks.shipping;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ShipmentMapper {
	private final JdbcTemplate jdbcTemplate;

	private final RowMapper<Shipment> shipmentRowMapper = (rs, i) -> new Shipment(
			Carrier.valueOf(rs.getString("carrier")),
			rs.getString("order_id"),
			rs.getDate("shipment_date").toLocalDate(),
			UUID.fromString(rs.getString("tracking_number")));

	public ShipmentMapper(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Optional<Shipment> findByOrderId(String orderId) {
		try {
			final Shipment shipment = this.jdbcTemplate.queryForObject("SELECT order_id, carrier, shipment_date, tracking_number FROM shipment WHERE order_id = ?", shipmentRowMapper, orderId);
			return Optional.ofNullable(shipment);
		}
		catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public List<Shipment> findAll() {
		return this.jdbcTemplate.query("SELECT order_id, carrier, shipment_date, tracking_number FROM shipment", this.shipmentRowMapper);
	}

	@Transactional
	public int insert(Shipment shipment) {
		return this.jdbcTemplate.update("INSERT INTO shipment(order_id, carrier, shipment_date, tracking_number) VALUES (?, ?, ?, ?)",
				shipment.orderId(),
				shipment.carrier().name(),
				shipment.shipmentDate(),
				shipment.trackingNumber().toString());
	}
}
