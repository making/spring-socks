package lol.maki.socks.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import lol.maki.socks.cart.client.CartApi;
import lol.maki.socks.payment.client.PaymentApi;
import lol.maki.socks.shipping.client.ShipmentApi;

import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServletBearerExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SockConfig {
	private final SockProps props;

	public SockConfig(SockProps props) {
		this.props = props;
	}

	@Bean
	public WebClient webClient(WebClient.Builder builder, LoadBalancedExchangeFilterFunction loadBalancedExchangeFilterFunction) {
		return builder
				.filter(new ServletBearerExchangeFilterFunction())
				.filter(loadBalancedExchangeFilterFunction)
				.filter(new LoggingExchangeFilterFunction(false))
				.build();
	}

	@Bean
	public CartApi cartApi(WebClient webClient, ObjectMapper objectMapper) {
		return new CartApi(new lol.maki.socks.cart.client.ApiClient(webClient, objectMapper, StdDateFormat.instance)
				.setBasePath(this.props.getCartUrl()));
	}

	@Bean
	public ShipmentApi shipmentApi(WebClient webClient, ObjectMapper objectMapper) {
		return new ShipmentApi(new lol.maki.socks.shipping.client.ApiClient(webClient, objectMapper, StdDateFormat.instance)
				.setBasePath(this.props.getShippingUrl()));
	}

	@Bean
	public PaymentApi paymentApi(WebClient webClient, ObjectMapper objectMapper) {
		return new PaymentApi(new lol.maki.socks.payment.client.ApiClient(webClient, objectMapper, StdDateFormat.instance)
				.setBasePath(this.props.getPaymentUrl()));
	}
}
