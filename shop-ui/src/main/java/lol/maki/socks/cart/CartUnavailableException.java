package lol.maki.socks.cart;

public class CartUnavailableException extends RuntimeException {
	public CartUnavailableException(Throwable cause) {
		super("Cart is currently unavailable. Retry later. Sorry for the inconvenience.", cause);
	}
}
