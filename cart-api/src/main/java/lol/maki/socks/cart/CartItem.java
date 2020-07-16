package lol.maki.socks.cart;

import java.math.BigDecimal;
import java.util.Objects;

public final class CartItem {
	private final String itemId;

	private int quantity;

	private BigDecimal unitPrice;

	public CartItem(String itemId, int quantity, BigDecimal unitPrice) {
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
		this.quantity = quantity;
		return this;
	}

	public CartItem incrementQuantity(int increase) {
		this.quantity += increase;
		return this;
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
