package lol.maki.socks.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "sock")
@ConstructorBinding
public class SockProps {
	private final String cartUrl;

	private final String paymentUrl;

	private final String shippingUrl;

	private final String userUrl;

	public SockProps(String cartUrl, String paymentUrl, String shippingUrl, String userUrl) {
		this.cartUrl = cartUrl;
		this.paymentUrl = paymentUrl;
		this.shippingUrl = shippingUrl;
		this.userUrl = userUrl;
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

	public String getUserUrl() {
		return userUrl;
	}
}
