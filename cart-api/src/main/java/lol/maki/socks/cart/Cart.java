package lol.maki.socks.cart;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Cart {
	private final String customerId;

	private final List<CartItem> items;

	public Cart(String customerId, List<CartItem> items) {
		this.customerId = customerId;
		this.items = items;
	}

	public Cart(String customerId) {
		this(customerId, new ArrayList<>());
	}

	public String customerId() {
		return this.customerId;
	}

	public List<CartItem> items() {
		return this.items;
	}

	public CartItem mergeItem(CartItem cartItem) {
		final Optional<CartItem> item = this.findItem(cartItem.itemId());
		return item.map(i -> {
			i
					.incrementQuantity(cartItem.quantity())
					.updateUnitPrice(cartItem.unitPrice());
			return i;
		}).orElseGet(() -> {
			this.items().add(cartItem);
			return cartItem;
		});
	}

	public CartItem replaceItem(CartItem cartItem) {
		final Optional<CartItem> item = this.findItem(cartItem.itemId());
		return item.map(i -> {
			i
					.updateQuantity(cartItem.quantity())
					.updateUnitPrice(cartItem.unitPrice());
			return i;
		}).orElseGet(() -> {
			this.items().add(cartItem);
			return cartItem;
		});
	}

	public Cart mergeCart(Cart another) {
		another.items().forEach(this::mergeItem);
		return this;
	}

	public Cart removeItem(String itemId) {
		this.items().removeIf(i -> Objects.equals(i.itemId(), itemId));
		return this;
	}

	public Optional<CartItem> findItem(String itemId) {
		return this.items().stream()
				.filter(i -> Objects.equals(i.itemId(), itemId))
				.findFirst();
	}

	@Override
	public String toString() {
		return "Cart{" +
				"customerId='" + customerId + '\'' +
				", items=" + items +
				'}';
	}
}
