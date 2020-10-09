package lol.maki.socks.order;

import java.net.URI;
import java.text.ParseException;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.jwt.SignedJWT;
import lol.maki.socks.cart.Cart;
import lol.maki.socks.config.SockProps;
import lol.maki.socks.order.client.OrderApi;
import lol.maki.socks.order.client.OrderRequest;
import lol.maki.socks.order.client.OrderResponse;
import reactor.core.publisher.Mono;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@Service
public class OrderService {
	private final OrderApi orderApi;

	private final WebClient webClient;

	private final SockProps props;

	public OrderService(OrderApi orderApi, Builder builder, SockProps props, ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
		this.orderApi = orderApi;
		this.webClient = builder
				.filter(new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager))
				.build();
		this.props = props;
	}

	public Mono<OrderResponse> placeOrder(Cart cart, Order order, OAuth2AuthorizedClient authorizedClient) {
		final ClientRegistration client = authorizedClient.getClientRegistration();
		final String username = UUID.randomUUID().toString();
		final String password = UUID.randomUUID().toString();
		return this.createUser(order, username, password, authorizedClient)
				.then(this.retrieveToken(username, password, client.getClientId(), client.getClientSecret())
						.flatMap(accessToken -> Mono.zip(this.createAddress(order, accessToken), this.createCard(order, accessToken))
								.flatMap(tpl -> this.createOrder(cart, customerId(accessToken), tpl.getT1(), tpl.getT2()))));
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

	Mono<OrderResponse> createOrder(Cart cart, String customerId, String addressId, String cardId) {
		final URI address = UriComponentsBuilder.fromHttpUrl(this.props.getUserUrl())
				.path("addresses/{addressId}")
				.build(addressId);
		final URI card = UriComponentsBuilder.fromHttpUrl(this.props.getUserUrl())
				.path("cards/{cardId}")
				.build(cardId);
		final URI customer = UriComponentsBuilder.fromHttpUrl(this.props.getUserUrl())
				.path("customers/{customerId}")
				.build(customerId);
		return this.orderApi.createOrder(new OrderRequest()
				.customer(customer)
				.address(address)
				.card(card)
				.items(UriComponentsBuilder.fromHttpUrl(props.getCartUrl())
						.pathSegment("carts/{cartId}/items")
						.build(cart.getCartId())));
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
}
