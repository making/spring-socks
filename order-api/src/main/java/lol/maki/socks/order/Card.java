package lol.maki.socks.order;

import java.time.Clock;
import java.time.LocalDate;

public class Card {
	private final String longNum;

	private final LocalDate expires;

	private final String ccv;

	public Card(String longNum, LocalDate expires, String ccv) {
		this.longNum = longNum;
		this.expires = expires;
		this.ccv = ccv;
	}

	public String longNum() {
		return longNum;
	}

	public LocalDate expires() {
		return expires;
	}

	public String ccv() {
		return ccv;
	}

	public final boolean isExpired(Clock clock) {
		final LocalDate now = LocalDate.now(clock);
		return now.isAfter(expires());
	}
}
