package lol.maki.socks.customer;

import java.net.URI;

import lol.maki.socks.config.SockProps;
import lol.maki.socks.user.client.CustomerAddressResponse;
import lol.maki.socks.user.client.CustomerCardResponse;
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

	public Mono<CustomerResponse> retrieveCustomer(String customerUri) {
		return this.webClient.get()
				.uri(this.props.getUserUrl(), b -> b.path("me").build())
				.retrieve()
				.bodyToMono(CustomerResponse.class);
	}

	public Mono<CustomerResponse> retrieveCustomerOld(URI customerUri) {
		return this.webClient.get()
				.uri(customerUri)
				.retrieve()
				.bodyToMono(CustomerResponse.class);
	}

	public Mono<CustomerAddressResponse> retrieveAddress(URI addressUri) {
		return this.webClient.get()
				.uri(addressUri)
				.retrieve()
				.bodyToMono(CustomerAddressResponse.class);
	}

	public Mono<CustomerCardResponse> retrieveCard(URI cardUri) {
		return this.webClient.get()
				.uri(cardUri)
				.retrieve()
				.bodyToMono(CustomerCardResponse.class);
	}
}
