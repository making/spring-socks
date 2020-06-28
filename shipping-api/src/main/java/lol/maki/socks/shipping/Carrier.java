package lol.maki.socks.shipping;

import java.time.Duration;
import java.time.LocalDate;

public enum Carrier {
	USPS(Duration.ofDays(5)), FEDEX(Duration.ofDays(1)), UPS(Duration.ofDays(3));

	private final Duration durationToDeliver;

	Carrier(Duration durationToDeliver) {
		this.durationToDeliver = durationToDeliver;
	}

	public static Carrier chooseByItemCount(int itemCount) {
		if (itemCount == 1) {
			return Carrier.FEDEX;
		}
		else if (itemCount <= 3) {
			return Carrier.UPS;
		}
		return Carrier.USPS;
	}

	public LocalDate deliveryDate(LocalDate shipmentDate) {
		return shipmentDate
				.plusDays(this.durationToDeliver.toDays());
	}
}
