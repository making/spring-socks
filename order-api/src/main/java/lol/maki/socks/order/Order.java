package lol.maki.socks.order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.immutables.value.Value.Immutable;

import static java.math.BigDecimal.ZERO;

@Immutable
public abstract class Order {
	public abstract String id();

	public abstract Customer customer();

	public abstract Address address();

	public abstract Card card();

	public abstract List<Item> items();

	public abstract Shipment shipment();

	public abstract OffsetDateTime date();

	public abstract OrderStatus status();

	public static String newOrderId(Supplier<UUID> idGenerator) {
		return idGenerator.get().toString().substring(0, 8);
	}

	public final BigDecimal total() {
		return this.items()
				.stream()
				.map(Item::subTotal)
				.reduce(ZERO, BigDecimal::add);
	}

	public final int itemCount() {
		return this.items()
				.stream()
				.mapToInt(Item::quantity)
				.sum();
	}
}
