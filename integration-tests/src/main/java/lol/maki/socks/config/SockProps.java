package lol.maki.socks.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "sock")
@ConstructorBinding
public class SockProps {
	private final String catalogUrl;

	private final String cartUrl;

	private final String orderUrl;

	private final String paymentUrl;

	private final String shippingUrl;

	private final String userUrl;

	private final String username;

	private final String password;

	private final String clientId;

	private final String clientSecret;

	public SockProps(String catalogUrl, String cartUrl, String orderUrl, String paymentUrl, String shippingUrl, String userUrl, String username, String password, String clientId, String clientSecret) {
		this.catalogUrl = catalogUrl;
		this.cartUrl = cartUrl;
		this.orderUrl = orderUrl;
		this.paymentUrl = paymentUrl;
		this.shippingUrl = shippingUrl;
		this.userUrl = userUrl;
		this.username = username;
		this.password = password;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
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

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}
}
