package lol.maki.socks.cart;

import java.math.BigDecimal;
import java.util.Objects;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.Arguments2;
import am.ik.yavi.arguments.Arguments3;
import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.builder.ArgumentsValidatorBuilder;

public final class CartItem {
	private final String itemId;

	private int quantity;

	private BigDecimal unitPrice;

	static final Arguments3Validator<String, Integer, BigDecimal, CartItem> validator = ArgumentsValidatorBuilder.of(CartItem::new)
			.builder(b -> b
					._string(Arguments1::arg1, "itemId", c -> c.notBlank())
					._integer(Arguments2::arg2, "quantity", c -> c.greaterThanOrEqual(1))
					._bigDecimal(Arguments3::arg3, "unitPrice", c -> c.greaterThan(BigDecimal.ZERO)))
			.build();

	public CartItem(String itemId, int quantity, BigDecimal unitPrice) {
		CartItem.validator.validateAndThrowIfInvalid(itemId, quantity, unitPrice);
		this.itemId = itemId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
	}

	public String itemId() {
		return this.itemId;
	}

	public int quantity() {
		return this.quantity;
	}

	public BigDecimal unitPrice() {
		return this.unitPrice;
	}

	public CartItem updateQuantity(int quantity) {
		this.quantity = atLeastZero(quantity);
		return this;
	}

	public CartItem incrementQuantity(int increase) {
		this.quantity = atLeastZero(this.quantity + increase);
		return this;
	}

	static int atLeastZero(int v) {
		return Math.max(v, 0);
	}

	public CartItem updateUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CartItem cartItem = (CartItem) o;
		return quantity == cartItem.quantity &&
				Objects.equals(itemId, cartItem.itemId) &&
				Objects.equals(unitPrice, cartItem.unitPrice);
	}

	@Override
	public int hashCode() {
		return Objects.hash(itemId, quantity, unitPrice);
	}

	@Override
	public String toString() {
		return "CartItem{" +
				"itemId='" + itemId + '\'' +
				", quantity=" + quantity +
				", unitPrice=" + unitPrice +
				'}';
	}
}
