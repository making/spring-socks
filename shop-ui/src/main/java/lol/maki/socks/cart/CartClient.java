package lol.maki.socks.cart;

import java.util.UUID;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lol.maki.socks.cart.client.CartItemRequest;
import lol.maki.socks.cart.client.CartItemResponse;
import lol.maki.socks.cart.client.CartResponse;
import lol.maki.socks.config.LoggingExchangeFilterFunction;
import lol.maki.socks.config.SockProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Component
public class CartClient {
	private final WebClient webClient;

	private final Logger log = LoggerFactory.getLogger(CartClient.class);

	protected CartClient(Builder builder, LoadBalancedExchangeFilterFunction loadBalancerExchangeFilterFunction, ReactiveOAuth2AuthorizedClientManager authorizedClientManager, SockProps props) {
		this.webClient = builder
				.filter(new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager))
				.filter(loadBalancerExchangeFilterFunction)
				.filter(new LoggingExchangeFilterFunction(false))
				.baseUrl(props.getCartUrl())
				.build();
	}

	@CircuitBreaker(name = "cart")
	public Mono<CartItemResponse> addCartItem(String cartId, CartItemRequest item) {
		return this.webClient.post()
				.uri("carts/{cartId}/items", cartId)
				.attributes(clientRegistrationId("sock"))
				.bodyValue(item)
				.retrieve()
				.bodyToMono(CartItemResponse.class);
	}

	@CircuitBreaker(name = "cart")
	public Mono<Void> deleteCartItem(String cartId, UUID itemId) {
		return this.webClient.delete()
				.uri("carts/{cartId}/items/{itemId}", cartId, itemId.toString())
				.attributes(clientRegistrationId("sock"))
				.retrieve()
				.toBodilessEntity()
				.then();
	}

	@CircuitBreaker(name = "cart")
	public Mono<Void> patchCartItem(String cartId, CartItemRequest item) {
		return this.webClient.patch()
				.uri("carts/{cartId}/items", cartId)
				.attributes(clientRegistrationId("sock"))
				.bodyValue(item)
				.retrieve()
				.toBodilessEntity()
				.then();
	}

	@CircuitBreaker(name = "cart", fallbackMethod = "fallbackCart")
	public Mono<Cart> findOneWithFallback(String cartId) {
		return this.webClient.get()
				.uri("carts/{cartId}", cartId)
				.attributes(clientRegistrationId("sock"))
				.retrieve()
				.bodyToMono(CartResponse.class)
				.map(c -> new Cart(cartId, c.getItems().stream()
						.map(i -> new CartItem(UUID.fromString(i.getItemId()), i.getQuantity(), i.getUnitPrice()))
						.collect(toUnmodifiableList())));
	}

	private Mono<ResponseEntity<Void>> mergeInternal(String customerId, String sessionId) {
		return this.webClient.get()
				.uri("carts/{customerId}/merge?sessionId={sessionId}", customerId, sessionId)
				.attributes(clientRegistrationId("sock"))
				.retrieve()
				.toBodilessEntity();
	}

	@CircuitBreaker(name = "cart")
	public Mono<Void> merge(String customerId, Cart cart) {
		if (!cart.hasSessionId()) {
			return Mono.empty();
		}
		return this.mergeInternal(customerId, cart.getCartId())
				.then();
	}

	public Mono<Void> mergeWithFallback(String customerId, String sessionId) {
		return this.mergeInternal(customerId, sessionId)
				.onErrorReturn(ResponseEntity.ok(null))
				.then();
	}

	Mono<Cart> fallbackCart(String cartId, Throwable throwable) {
		return Mono.fromCallable(() -> Cart.empty(cartId));
	}
}
