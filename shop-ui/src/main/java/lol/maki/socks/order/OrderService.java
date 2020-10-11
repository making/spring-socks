package lol.maki.socks.order;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import lol.maki.socks.cart.Cart;
import lol.maki.socks.config.SockProps;
import lol.maki.socks.order.client.OrderRequest;
import lol.maki.socks.order.client.OrderResponse;
import lol.maki.socks.payment.client.AuthorizationResponse;
import lol.maki.socks.security.ShopUser;
import reactor.core.publisher.Mono;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@Service
public class OrderService {
	private final WebClient webClient;

	private final SockProps props;

	private final ObjectMapper objectMapper;

	public OrderService(Builder builder, SockProps props, ReactiveOAuth2AuthorizedClientManager authorizedClientManager, ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.webClient = builder
				.filter(new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager))
				.build();
		this.props = props;
	}

	public Mono<OrderResponse> placeOrderWithLogin(ShopUser shopUser, Cart cart, Order order, OAuth2AuthorizedClient authorizedClient) {
		final String customerId = shopUser.getSubject();
		final String accessToken = shopUser.getAccessToken().getTokenValue();
		return Mono.zip(this.createAddress(order, accessToken), this.createCard(order, accessToken))
				.flatMap(tpl ->
						this.mergeCart(customerId, cart, authorizedClient)
								.then(this.createOrder(customerId, tpl.getT1(), tpl.getT2(), accessToken)));
	}

	public Mono<OrderResponse> placeOrderWithoutLogin(Cart cart, Order order, OAuth2AuthorizedClient authorizedClient) {
		final ClientRegistration client = authorizedClient.getClientRegistration();
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		return this.createUser(order, username, password, authorizedClient)
				.then(this.retrieveToken(username, password, client.getClientId(), client.getClientSecret())
						.flatMap(accessToken -> {
							final String customerId = customerId(accessToken);
							return Mono.zip(this.createAddress(order, accessToken), this.createCard(order, accessToken))
									.flatMap(tpl ->
											this.mergeCart(customerId, cart, authorizedClient)
													.then(this.createOrder(customerId, tpl.getT1(), tpl.getT2(), accessToken)));
						}));
	}

	Mono<?> createUser(Order order, String username, String password, OAuth2AuthorizedClient authorizedClient) {
		return this.webClient.post()
				.uri(props.getUserUrl(), b -> b.path("register").build())
				.bodyValue(Map.of("username", username,
						"password", password,
						"firstName", order.getFirstName(),
						"lastName", order.getLastName(),
						"email", order.getEmail(),
						"allowDuplicateEmail", String.valueOf(!order.isCreateAccount())))
				.attributes(oauth2AuthorizedClient(authorizedClient))
				.retrieve()
				.toBodilessEntity();
	}

	Mono<String> retrieveToken(String username, String password, String clientId, String clientSecret) {
		return this.webClient.post()
				.uri(props.getUserUrl(), b -> b.path("oauth/token").build())
				.headers(httpHeaders -> httpHeaders.setBasicAuth(clientId, clientSecret))
				.bodyValue(new LinkedMultiValueMap<>() {
					{
						add("grant_type", "password");
						add("username", username);
						add("password", password);
					}
				})
				.retrieve()
				.bodyToMono(JsonNode.class)
				.map(n -> n.get("access_token").asText());
	}

	Mono<?> mergeCart(String customerId, Cart cart, OAuth2AuthorizedClient authorizedClient) {
		if (!cart.hasSessionId()) {
			return Mono.empty();
		}
		return this.webClient.get()
				.uri(props.getCartUrl(), b -> b.path("carts/{customerId}/merge")
						.queryParam("sessionId", cart.getCartId())
						.build(customerId))
				.attributes(oauth2AuthorizedClient(authorizedClient))
				.retrieve()
				.toBodilessEntity();
	}

	Mono<String> createAddress(Order order, String accessToken) {
		return this.webClient.post()
				.uri(props.getUserUrl(), b -> b.path("addresses").build())
				.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
				.bodyValue(Map.of("number", order.getNumber(),
						"street", order.getStreet(),
						"city", order.getCity(),
						"postcode", order.getPostcode(),
						"country", order.getCountry()
				))
				.retrieve()
				.bodyToMono(JsonNode.class)
				.map(n -> n.get("addressId").asText());
	}

	Mono<String> createCard(Order order, String accessToken) {
		return this.webClient.post()
				.uri(props.getUserUrl(), b -> b.path("cards").build())
				.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
				.bodyValue(Map.of("longNum", order.getLongNum(),
						"expires", order.getExpires(),
						"ccv", order.getCcv()
				))
				.retrieve()
				.bodyToMono(JsonNode.class)
				.map(n -> n.get("cardId").asText());
	}

	Mono<OrderResponse> createOrder(String customerId, String addressId, String cardId, String accessToken) {
		final URI address = UriComponentsBuilder.fromHttpUrl(this.props.getUserUrl())
				.path("addresses/{addressId}")
				.build(addressId);
		final URI card = UriComponentsBuilder.fromHttpUrl(this.props.getUserUrl())
				.path("cards/{cardId}")
				.build(cardId);
		final URI customer = UriComponentsBuilder.fromHttpUrl(this.props.getUserUrl())
				.path("customers/{customerId}")
				.build(customerId);
		return this.webClient.post()
				.uri(this.props.getOrderUrl(), b -> b.path("orders").build())
				.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
				.bodyValue(new OrderRequest()
						.customer(customer)
						.address(address)
						.card(card)
						.items(UriComponentsBuilder.fromHttpUrl(props.getCartUrl())
								.pathSegment("carts/{customerId}/items")
								.build(customerId)))
				.retrieve()
				.bodyToMono(OrderResponse.class)
				.onErrorMap(WebClientResponseException.class, e -> {
					if (e.getStatusCode() == CONFLICT) {
						throw this.convertPaymentException(e);
					}
					throw e;
				});
	}

	String customerId(String accessToken) {
		try {
			final SignedJWT jwt = SignedJWT.parse(accessToken);
			return jwt.getJWTClaimsSet().getSubject();
		}
		catch (ParseException e) {
			throw new IllegalStateException(e);
		}
	}

	RuntimeException convertPaymentException(WebClientResponseException e) {
		try {
			final AuthorizationResponse authorizationResponse = this.objectMapper.readValue(e.getResponseBodyAsByteArray(), AuthorizationResponse.class);
			throw new IllegalStateException(authorizationResponse.getAuthorization().getMessage(), e);
		}
		catch (IOException ignored) {
			throw e;
		}
	}
}
