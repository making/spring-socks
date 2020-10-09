package lol.maki.socks.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "sock")
@ConstructorBinding
public class SockProps {
	private final String testCustomerId;

	private final String catalogUrl;

	private final String cartUrl;

	private final String orderUrl;

	private final String paymentUrl;

	private final String shippingUrl;

	private final String userUrl;

	public SockProps(String testCustomerId, String catalogUrl, String cartUrl, String orderUrl, String paymentUrl, String shippingUrl, String userUrl) {
		this.testCustomerId = testCustomerId;
		this.catalogUrl = catalogUrl;
		this.cartUrl = cartUrl;
		this.orderUrl = orderUrl;
		this.paymentUrl = paymentUrl;
		this.shippingUrl = shippingUrl;
		this.userUrl = userUrl;
	}

	public String getTestCustomerId() {
		return testCustomerId;
	}

	public String getCatalogUrl() {
		return catalogUrl;
	}

	public String getCartUrl() {
		return cartUrl;
	}

	public String getOrderUrl() {
		return orderUrl;
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
