package lol.maki.socks.order;

import java.time.LocalDate;
import java.util.UUID;

public class Shipment {
	private final String carrier;

	private final UUID trackingNumber;

	private final LocalDate deliveryDate;

	public Shipment(String carrier, UUID trackingNumber, LocalDate deliveryDate) {
		this.carrier = carrier;
		this.trackingNumber = trackingNumber;
		this.deliveryDate = deliveryDate;
	}

	public String carrier() {
		return carrier;
	}

	public UUID trackingNumber() {
		return trackingNumber;
	}

	public LocalDate deliveryDate() {
		return deliveryDate;
	}
}
