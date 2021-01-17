package lol.maki.socks.customer;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Card implements Serializable {
	private static final long serialVersionUID = 1L;

	private final UUID cardId;

	private final String longNum;

	private final LocalDate expires;

	private final String ccv;

	public Card(UUID cardId, String longNum, LocalDate expires, String ccv) {
		this.cardId = cardId;
		this.longNum = longNum;
		this.expires = expires;
		this.ccv = ccv;
	}

	public static long serialVersionUID() {
		return serialVersionUID;
	}

	public UUID cardId() {
		return cardId;
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

	public boolean isSame(Card card) {
		return Objects.equals(this.longNum(), card.longNum()) &&
				Objects.equals(this.expires(), card.expires()) &&
				Objects.equals(this.ccv(), card.ccv());
	}
}
