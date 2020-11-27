package lol.maki.socks.cart;

import java.util.UUID;

import lol.maki.socks.cart.client.CartResponse;
import lol.maki.socks.config.SockProps;
import reactor.core.publisher.Mono;

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

	protected CartClient(Builder builder, ReactiveOAuth2AuthorizedClientManager authorizedClientManager, SockProps props) {
		this.webClient = builder.filter(new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager))
				.baseUrl(props.getCartUrl())
				.build();
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
}
