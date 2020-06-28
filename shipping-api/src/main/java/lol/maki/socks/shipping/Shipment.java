package lol.maki.socks.shipping;

import java.time.LocalDate;
import java.util.UUID;

import org.immutables.value.Value.Immutable;

@Immutable
public abstract class Shipment {
	public abstract Carrier carrier();

	public abstract String orderId();

	public abstract LocalDate shipmentDate();

	public abstract UUID trackingNumber();
}
