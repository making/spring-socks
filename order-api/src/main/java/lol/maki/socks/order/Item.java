package lol.maki.socks.order;

import java.math.BigDecimal;

public class Item {
	private final String orderId;

	private final String itemId;

	private final int quantity;

	private final BigDecimal unitPrice;

	public Item(String orderId, String itemId, int quantity, BigDecimal unitPrice) {
		this.orderId = orderId;
		this.itemId = itemId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
	}

	public String orderId() {
		return orderId;
	}

	public String itemId() {
		return itemId;
	}

	public int quantity() {
		return quantity;
	}

	public BigDecimal unitPrice() {
		return unitPrice;
	}

	public final BigDecimal subTotal() {
		return unitPrice().multiply(BigDecimal.valueOf(quantity()));
	}
}
