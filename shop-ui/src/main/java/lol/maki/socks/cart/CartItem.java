package lol.maki.socks.cart;

import java.math.BigDecimal;
import java.util.UUID;

public class CartItem {

	private final UUID itemId;

	private final Integer quantity;

	private final BigDecimal unitPrice;

	private String name = null;

	private String imageUrl = null;

	public CartItem(UUID itemId, Integer quantity, BigDecimal unitPrice) {
		this.itemId = itemId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
	}

	public CartItem(UUID itemId, Integer quantity, BigDecimal unitPrice, String name, String imageUrl) {
		this.itemId = itemId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.name = name;
		this.imageUrl = imageUrl;
	}

	public CartItem setNameAndImageUrl(String name, String imageUrl) {
		return new CartItem(this.itemId, this.quantity, this.unitPrice, name, imageUrl);
	}

	public UUID getItemId() {
		return itemId;
	}

	public String getName() {
		return name;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public BigDecimal getTotal() {
		return this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
	}

	@Override
	public String toString() {
		return "AddCartItemForm{" +
				"name='" + name + '\'' +
				", imageUrl='" + imageUrl + '\'' +
				", quantity=" + quantity +
				", unitPrice=" + unitPrice +
				'}';
	}
}
