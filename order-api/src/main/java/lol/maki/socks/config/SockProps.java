package lol.maki.socks.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "sock")
@ConstructorBinding
public class SockProps {
	private final String cartUrl;

	private final String paymentUrl;

	private final String shippingUrl;

	public SockProps(String cartUrl, String paymentUrl, String shippingUrl) {
		this.cartUrl = cartUrl;
		this.paymentUrl = paymentUrl;
		this.shippingUrl = shippingUrl;
	}

	public String getCartUrl() {
		return cartUrl;
	}

	public String getPaymentUrl() {
		return paymentUrl;
	}

	public String getShippingUrl() {
		return shippingUrl;
	}
}
