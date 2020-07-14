package lol.maki.socks.order;

import java.time.Clock;
import java.time.LocalDate;

import org.immutables.value.Value.Immutable;

@Immutable
public abstract class Card {
	public abstract String longNum();

	public abstract LocalDate expires();

	public abstract String ccv();

	public final boolean isExpired(Clock clock) {
		final LocalDate now = LocalDate.now(clock);
		return now.isAfter(expires());
	}
}
