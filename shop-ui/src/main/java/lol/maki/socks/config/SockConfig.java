package lol.maki.socks.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import lol.maki.socks.catalog.client.CatalogApi;
import lol.maki.socks.order.client.OrderApi;

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
	public CatalogApi catalogApi(WebClient.Builder builder, ObjectMapper objectMapper) {
		return new CatalogApi(new lol.maki.socks.catalog.client.ApiClient(builder.build(), objectMapper, StdDateFormat.instance)
				.setBasePath(this.props.getCatalogUrl()));
	}

	@Bean
	public OrderApi orderApi(WebClient.Builder builder, ObjectMapper objectMapper) {
		return new OrderApi(new lol.maki.socks.order.client.ApiClient(builder.build(), objectMapper, StdDateFormat.instance)
				.setBasePath(this.props.getOrderUrl()));
	}
}
