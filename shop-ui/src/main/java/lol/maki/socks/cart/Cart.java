package lol.maki.socks.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static java.math.BigDecimal.ZERO;

public class Cart {
	private final String cartId;

	private final List<CartItem> cartItems;

	public Cart(String cartId, List<CartItem> cartItems) {
		this.cartId = cartId;
		this.cartItems = cartItems;
	}

	public boolean hasSessionId() {
		return this.cartId.startsWith("_");
	}

	public static Cart empty(String cartId) {
		return new Cart(cartId, new ArrayList<>());
	}

	public static String generateSessionId(Supplier<UUID> uuidgen) {
		return "_" + uuidgen.get();
	}

	public String getCartId() {
		return this.cartId;
	}

	public int getItemSize() {
		return this.cartItems.stream().mapToInt(CartItem::getQuantity).sum();
	}

	public List<CartItem> getItems() {
		return this.cartItems;
	}

	public BigDecimal getTotal() {
		return this.cartItems
				.stream()
				.map(CartItem::getTotal)
				.reduce(ZERO, BigDecimal::add);
	}

	@Override
	public String toString() {
		return "Cart{" +
				"cartItems=" + cartItems +
				'}';
	}
}
