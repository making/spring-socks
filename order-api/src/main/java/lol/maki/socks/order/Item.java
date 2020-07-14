package lol.maki.socks.order;

import java.math.BigDecimal;

import org.immutables.value.Value.Immutable;

@Immutable
public abstract class Item {
	public abstract String orderId();

	public abstract String itemId();

	public abstract int quantity();

	public abstract BigDecimal unitPrice();

	public final BigDecimal subTotal() {
		return unitPrice().multiply(BigDecimal.valueOf(quantity()));
	}
}
