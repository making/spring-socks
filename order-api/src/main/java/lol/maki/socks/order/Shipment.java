package lol.maki.socks.order;

import java.time.LocalDate;
import java.util.UUID;

import org.immutables.value.Value.Immutable;

@Immutable
public abstract class Shipment {
	public abstract String carrier();

	public abstract UUID trackingNumber();

	public abstract LocalDate deliveryDate();
}
