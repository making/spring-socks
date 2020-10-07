package lol.maki.socks.customer;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import org.immutables.value.Value.Immutable;

@Immutable
public abstract class Card {
	public abstract UUID cardId();

	public abstract String longNum();

	public abstract LocalDate expires();

	public abstract String ccv();

	public boolean isSame(Card card) {
		return Objects.equals(this.longNum(), card.longNum()) &&
				Objects.equals(this.expires(), card.expires()) &&
				Objects.equals(this.ccv(), card.ccv());
	}
}
