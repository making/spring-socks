package lol.maki.socks.customer;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lol.maki.socks.config.SockProps;
import lol.maki.socks.user.client.CustomerResponse;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CustomerClient {
	private final WebClient webClient;

	private final SockProps props;

	public CustomerClient(WebClient webClient, SockProps props) {
		this.webClient = webClient;
		this.props = props;
	}

	@CircuitBreaker(name = "user")
	public Mono<CustomerResponse> retrieveCustomer(String customerUri) {
		return this.webClient.get()
				.uri(this.props.getUserUrl(), b -> b.path("me").build())
				.retrieve()
				.bodyToMono(CustomerResponse.class);
	}
}
