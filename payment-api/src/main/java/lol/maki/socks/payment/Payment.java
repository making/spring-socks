package lol.maki.socks.payment;

import java.math.BigDecimal;

public class Payment {
	private final BigDecimal amount;

	private final BigDecimal declineOverAmount;

	public Payment(BigDecimal amount, BigDecimal declineOverAmount) {
		this.amount = amount;
		this.declineOverAmount = declineOverAmount;
	}

	public BigDecimal amount() {
		return amount;
	}

	public BigDecimal declineOverAmount() {
		return declineOverAmount;
	}

	public AuthorizationResult invalidPaymentAmount() {
		return invalidPaymentAmount;
	}

	private final AuthorizationResult invalidPaymentAmount = new AuthorizationResult(
			false,
			false,
			"Invalid payment amount");

	public final AuthorizationResult authorize() {
		if (amount().compareTo(BigDecimal.ZERO) <= 0) {
			return this.invalidPaymentAmount;
		}
		if (amount().compareTo(declineOverAmount()) <= 0) {
			return new AuthorizationResult(
					true,
					true,
					"Payment authorised");
		}
		else {
			return new AuthorizationResult(
					false,
					true,
					String.format("Payment declined: amount exceeds %.2f", declineOverAmount()));
		}
	}
}
