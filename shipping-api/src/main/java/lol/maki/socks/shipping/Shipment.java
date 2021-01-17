package lol.maki.socks.shipping;

import java.time.LocalDate;
import java.util.UUID;

public class Shipment {
	private final Carrier carrier;

	private final String orderId;

	private final LocalDate shipmentDate;

	private final UUID trackingNumber;

	public Shipment(Carrier carrier, String orderId, LocalDate shipmentDate, UUID trackingNumber) {
		this.carrier = carrier;
		this.orderId = orderId;
		this.shipmentDate = shipmentDate;
		this.trackingNumber = trackingNumber;
	}

	public Carrier carrier() {
		return carrier;
	}

	public String orderId() {
		return orderId;
	}

	public LocalDate shipmentDate() {
		return shipmentDate;
	}

	public UUID trackingNumber() {
		return trackingNumber;
	}
}
