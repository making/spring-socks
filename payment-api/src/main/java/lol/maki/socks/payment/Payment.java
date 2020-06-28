package lol.maki.socks.payment;

import org.immutables.value.Value.Immutable;

@Immutable
public abstract class Payment {
	public abstract float amount();

	public abstract float declineOverAmount();

	private final AuthorizationResult invalidPaymentAmount = ImmutableAuthorizationResult.builder()
			.authorized(false)
			.valid(false)
			.message("Invalid payment amount")
			.build();

	public final AuthorizationResult authorize() {
		if (amount() <= 0) {
			return this.invalidPaymentAmount;
		}
		if (amount() <= declineOverAmount()) {
			return ImmutableAuthorizationResult.builder()
					.authorized(true)
					.valid(true)
					.message("Payment authorised")
					.build();
		}
		else {
			return ImmutableAuthorizationResult.builder()
					.authorized(false)
					.valid(true)
					.message(String.format("Payment declined: amount exceeds %.2f", declineOverAmount()))
					.build();
		}
	}
}
