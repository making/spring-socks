package lol.maki.socks.cart;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lol.maki.socks.catalog.client.SockResponse;
import lol.maki.socks.config.LoggingExchangeFilterFunction;
import lol.maki.socks.config.SockProps;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Service
public class CartService {
	private final WebClient webClient;

	protected CartService(Builder builder, LoadBalancedExchangeFilterFunction loadBalancerExchangeFilterFunction, ReactiveOAuth2AuthorizedClientManager authorizedClientManager, SockProps props) {
		this.webClient = builder
				.filter(new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager))
				.filter(loadBalancerExchangeFilterFunction)
				.filter(new LoggingExchangeFilterFunction(false))
				.baseUrl(props.getCatalogUrl())
				.build();
	}

	@CircuitBreaker(name = "cart", fallbackMethod = "fallbackCart")
	public Mono<Cart> retrieveLatest(Cart cart) {
		return Flux.fromIterable(cart.getItems())
				.flatMap(i -> webClient.get()
						.uri("catalogue/{id}", i.getItemId())
						.attributes(clientRegistrationId("sock"))
						.retrieve()
						.bodyToMono(SockResponse.class)
						.map(s -> i.setNameAndImageUrl(s.getName(), s.getImageUrl().get(0))))
				.collectList()
				.map(items -> new Cart(cart.getCartId(), items));
	}

	Mono<Cart> fallbackCart(Cart cart, Throwable throwable) {
		return Mono.fromCallable(() -> Cart.empty(cart.getCartId()));
	}
}
