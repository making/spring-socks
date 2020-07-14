package lol.maki.socks.order;

public class PaymentUnauthorizedException extends RuntimeException {
	public PaymentUnauthorizedException(String message) {
		super(message);
	}
}
