package lol.maki.socks.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import lol.maki.socks.payment.client.PaymentApi;
import lol.maki.socks.shipping.client.ShipmentApi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SockConfig {
	private final SockProps props;

	public SockConfig(SockProps props) {
		this.props = props;
	}

	@Bean
	public ShipmentApi shipmentApi(WebClient.Builder builder, ObjectMapper objectMapper) {
		return new ShipmentApi(new lol.maki.socks.shipping.client.ApiClient(builder.build(), objectMapper, StdDateFormat.instance)
				.setBasePath(this.props.getShippingUrl()));
	}

	@Bean
	public PaymentApi paymentApi(WebClient.Builder builder, ObjectMapper objectMapper) {
		return new PaymentApi(new lol.maki.socks.payment.client.ApiClient(builder.build(), objectMapper, StdDateFormat.instance)
				.setBasePath(this.props.getPaymentUrl()));
	}
}
