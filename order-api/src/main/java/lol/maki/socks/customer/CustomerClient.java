package lol.maki.socks.customer;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lol.maki.socks.config.LoggingExchangeFilterFunction;
import lol.maki.socks.config.SockProps;
import lol.maki.socks.user.client.CustomerResponse;
import reactor.core.publisher.Mono;

import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServletBearerExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CustomerClient {
	private final WebClient webClient;

	public CustomerClient(WebClient.Builder builder, SockProps props) {
		this.webClient = builder
				.filter(new ServletBearerExchangeFilterFunction())
				.filter(new LoggingExchangeFilterFunction(false))
				.baseUrl(props.getUserUrl())
				.build();
	}

	@CircuitBreaker(name = "user")
	public Mono<CustomerResponse> retrieveCustomer(String customerUri) {
		return this.webClient.get()
				.uri("me")
				.retrieve()
				.bodyToMono(CustomerResponse.class);
	}
}
