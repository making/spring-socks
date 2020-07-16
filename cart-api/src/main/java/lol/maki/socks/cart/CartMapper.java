package lol.maki.socks.cart;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class CartMapper {
	private final JdbcTemplate jdbcTemplate;

	public CartMapper(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional
	public int insertCart(Cart cart) {
		return 0;
	}

	@Transactional
	public int updateCart(Cart cart) {
		return 0;
	}

	@Transactional
	public int deleteCartByCustomerId(String customerId) {
		return 0;
	}

	public Optional<Cart> findCartByCustomerId(String customerId) {
		return Optional.of(new Cart(customerId, new ArrayList<>() {
			{
				add(new CartItem("1", 1, BigDecimal.TEN));
			}
		}));
	}

	public Optional<CartItem> findCartItemByCustomerIdAndItemId(String customerId, String itemId) {
		return Optional.of(new CartItem("1", 1, BigDecimal.TEN));
	}
}
