package lol.maki.socks.cart;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.Arguments2;
import am.ik.yavi.arguments.Arguments2Validator;
import am.ik.yavi.builder.ArgumentsValidatorBuilder;

public final class Cart {
	private final String customerId;

	private final List<CartItem> items;

	static final Arguments2Validator<String, List<CartItem>, Cart> validator = ArgumentsValidatorBuilder
			.<String, List<CartItem>, Cart>of(Cart::new)
			.builder(b -> b
					._string(Arguments1::arg1, "customerId", c -> c.notBlank())
					._collection(Arguments2::arg2, "items", c -> c.notNull()))
			.build();

	public Cart(String customerId, List<CartItem> items) {
		Cart.validator.validateAndThrowIfInvalid(customerId, items);
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
