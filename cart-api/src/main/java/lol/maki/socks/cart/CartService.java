package lol.maki.socks.cart;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {
	private final CartMapper cartMapper;

	public CartService(CartMapper cartMapper) {
		this.cartMapper = cartMapper;
	}

	@Transactional
	public Cart getOrCreateCart(String customerId) {
		return this.cartMapper.findCartByCustomerId(customerId)
				.orElseGet(() -> {
					final Cart cart = new Cart(customerId);
					this.cartMapper.insertCart(cart);
					return cart;
				});
	}

	@Transactional
	public CartItem mergeCartItem(String customerId, CartItem cartItem) {
		final Cart cart = this.getOrCreateCart(customerId);
		final CartItem updated = cart.mergeItem(cartItem);
		this.cartMapper.upsertCartItems(cart);
		return updated;
	}

	@Transactional
	public CartItem replaceCartItem(String customerId, CartItem cartItem) {
		final Cart cart = this.getOrCreateCart(customerId);
		final CartItem updated = cart.replaceItem(cartItem);
		this.cartMapper.upsertCartItems(cart);
		return updated;
	}

	@Transactional
	public void deleteCartItem(String customerId, String itemId) {
		this.getOrCreateCart(customerId).removeItem(itemId);
		this.cartMapper.deleteCartByCustomerIdAndItemId(customerId, itemId);
	}

	@Transactional
	public Optional<Cart> mergeCart(String customerId, String sessionId) {
		final Cart cart = this.getOrCreateCart(customerId);
		final Optional<Cart> sessionCart = this.cartMapper.findCartByCustomerId(sessionId);
		return sessionCart.map(cart::mergeCart)
				.map(c -> {
					this.cartMapper.upsertCartItems(c);
					this.cartMapper.deleteCartByCustomerId(sessionId);
					return c;
				});
	}
}
