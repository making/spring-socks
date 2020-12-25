package lol.maki.socks.cart;

import java.util.UUID;
import java.util.function.Function;

import lol.maki.socks.cart.client.CartItemRequest;
import lol.maki.socks.cart.client.CartItemResponse;
import lol.maki.socks.cart.client.CartResponse;
import lol.maki.socks.config.LoggingExchangeFilterFunction;
import lol.maki.socks.config.SockProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Component
public class CartClient {
	private final WebClient webClient;

	private final Logger log = LoggerFactory.getLogger(CartClient.class);

	protected CartClient(Builder builder, ReactiveOAuth2AuthorizedClientManager authorizedClientManager, SockProps props) {
		this.webClient = builder
				.filter(new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager))
				.filter(LoggingExchangeFilterFunction.SINGLETON)
				.baseUrl(props.getCartUrl())
				.build();
	}

	public Mono<CartItemResponse> addCartItem(String cartId, CartItemRequest item) {
		return this.webClient.post()
				.uri("carts/{cartId}/items", cartId)
				.attributes(clientRegistrationId("sock"))
				.bodyValue(item)
				.retrieve()
				.bodyToMono(CartItemResponse.class)
				.onErrorResume(this.handleException());
	}

	public Mono<Void> deleteCartItem(String cartId, UUID itemId) {
		return this.webClient.delete()
				.uri("carts/{cartId}/items/{itemId}", cartId, itemId.toString())
				.attributes(clientRegistrationId("sock"))
				.retrieve()
				.toBodilessEntity()
				.then()
				.onErrorResume(this.handleException());
	}

	public Mono<Void> patchCartItem(String cartId, CartItemRequest item) {
		return this.webClient.patch()
				.uri("carts/{cartId}/items", cartId)
				.attributes(clientRegistrationId("sock"))
				.bodyValue(item)
				.retrieve()
				.toBodilessEntity()
				.then()
				.onErrorResume(this.handleException());
	}

	public Mono<Cart> findOneWithFallback(String cartId) {
		return this.webClient.get()
				.uri("carts/{cartId}", cartId)
				.attributes(clientRegistrationId("sock"))
				.retrieve()
				.bodyToMono(CartResponse.class)
				.map(c -> new Cart(cartId, c.getItems().stream()
						.map(i -> new CartItem(UUID.fromString(i.getItemId()), i.getQuantity(), i.getUnitPrice()))
						.collect(toUnmodifiableList())))
				.onErrorReturn(Cart.empty(cartId));
	}

	public Mono<Void> mergeWithFallback(String customerId, String sessionId) {
		return this.webClient.get()
				.uri("carts/{customerId}/merge?sessionId={sessionId}", customerId, sessionId)
				.attributes(clientRegistrationId("sock"))
				.retrieve()
				.toBodilessEntity()
				.onErrorReturn(ResponseEntity.ok(null))
				.then();
	}

	<T> Function<Throwable, Mono<T>> handleException() {
		return throwable -> {
			log.warn("Failed to call cart-api.", throwable);
			if (throwable instanceof WebClientRequestException || (throwable instanceof WebClientResponseException && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError())) {
				return Mono.error(new CartUnavailableException(throwable));
			}
			else {
				return Mono.error(throwable);
			}
		};
	}
}
