package lol.maki.socks.payment;


public class AuthorizationResult {
	private final boolean authorized;

	private final boolean valid;

	private final String message;

	public AuthorizationResult(boolean authorized, boolean valid, String message) {
		this.authorized = authorized;
		this.valid = valid;
		this.message = message;
	}

	public boolean authorized() {
		return authorized;
	}

	public boolean valid() {
		return valid;
	}

	public String message() {
		return message;
	}
}
