package lol.maki.socks.cart;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import static java.util.stream.Collectors.toUnmodifiableList;

@Repository
public class CartMapper {
	private final JdbcTemplate jdbcTemplate;

	private final RowMapper<CartItem> cartItemRowMapper = (rs, i) -> {
		final String itemId = rs.getString("item_id");
		if (itemId == null) {
			return null;
		}
		return new CartItem(itemId, rs.getInt("quantity"), rs.getBigDecimal("unit_price"));
	};

	public CartMapper(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional
	public int[] insertCart(Cart cart) {
		final int cartUpdated = this.jdbcTemplate.update("INSERT INTO cart(customer_id) VALUES (?)", cart.customerId());
		int itemUpdated = 0;
		if (!CollectionUtils.isEmpty(cart.items())) {
			final List<Object[]> args = cart.items().stream()
					.map(i -> new Object[] { cart.customerId(), i.itemId(), i.quantity(), i.unitPrice() })
					.collect(toUnmodifiableList());
			final int[] batchUpdated = this.jdbcTemplate.batchUpdate("INSERT INTO cart_item(customer_id, item_id, quantity, unit_price) VALUES (?, ?, ?, ?)", args);
			itemUpdated = Arrays.stream(batchUpdated).sum();
		}
		return new int[] { cartUpdated, itemUpdated };
	}

	@Transactional
	public int upsertCartItems(Cart cart) {
		int itemUpdated = 0;
		final String customerId = cart.customerId();
		if (!CollectionUtils.isEmpty(cart.items())) {
			final List<Object[]> args = cart.items().stream()
					.map(i -> new Object[] { customerId, i.itemId(), i.quantity(), i.unitPrice(), i.quantity(), i.unitPrice() })
					.collect(toUnmodifiableList());
			final int[] batchUpdated = this.jdbcTemplate.batchUpdate("INSERT INTO cart_item(customer_id, item_id, quantity, unit_price) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE quantity = ?, unit_price = ?", args);
			itemUpdated = Arrays.stream(batchUpdated).sum();
		}
		else {
			this.jdbcTemplate.update("DELETE FROM cart_item WHERE customer_id = ?", customerId);
		}
		return itemUpdated;
	}

	@Transactional
	public int deleteCartByCustomerId(String customerId) {
		return this.jdbcTemplate.update("DELETE FROM cart WHERE customer_id = ?", customerId);
	}

	@Transactional
	public int deleteCartByCustomerIdAndItemId(String customerId, String itemId) {
		return this.jdbcTemplate.update("DELETE FROM cart_item WHERE customer_id = ? AND item_id = ?", customerId, itemId);
	}

	public Optional<Cart> findCartByCustomerId(String customerId) {
		try {
			final Cart cart = this.jdbcTemplate.query("SELECT c.customer_id, i.item_id, i.quantity, i.unit_price FROM cart AS c LEFT JOIN cart_item AS i ON c.customer_id = i.customer_id WHERE c.customer_id = ? FOR UPDATE",
					rs -> {
						Cart c = null;
						while (rs.next()) {
							if (c == null) {
								c = new Cart(rs.getString("customer_id"));
							}
							final CartItem cartItem = this.cartItemRowMapper.mapRow(rs, -1);
							if (cartItem != null) {
								c.mergeItem(cartItem);
							}
						}
						return c;
					},
					customerId);
			return Optional.ofNullable(cart);
		}
		catch (EmptyResultDataAccessException ignored) {
			return Optional.empty();
		}
	}

	public Optional<CartItem> findCartItemByCustomerIdAndItemId(String customerId, String itemId) {
		try {
			final CartItem cartItem = this.jdbcTemplate.queryForObject("SELECT i.item_id, i.quantity, i.unit_price FROM cart_item AS i LEFT JOIN cart c ON i.customer_id = c.customer_id WHERE c.customer_id = ? AND i.item_id = ?",
					this.cartItemRowMapper,
					customerId,
					itemId);
			return Optional.ofNullable(cartItem);
		}
		catch (EmptyResultDataAccessException ignored) {
			return Optional.empty();
		}
	}
}
