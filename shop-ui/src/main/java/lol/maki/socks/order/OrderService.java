package lol.maki.socks.order;

import java.text.ParseException;
import java.util.UUID;

import com.nimbusds.jwt.SignedJWT;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lol.maki.socks.cart.Cart;
import lol.maki.socks.cart.CartClient;
import lol.maki.socks.order.client.OrderResponse;
import lol.maki.socks.security.ShopUser;
import lol.maki.socks.user.UserClient;
import lol.maki.socks.user.client.CustomerAddressCreateRequest;
import lol.maki.socks.user.client.CustomerCardCreateRequest;
import lol.maki.socks.user.client.CustomerCreateRequest;
import reactor.core.publisher.Mono;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
	private final OrderClient orderClient;

	private final UserClient userClient;

	private final CartClient cartClient;


	public OrderService(OrderClient orderClient, UserClient userClient, CartClient cartClient) {
		this.orderClient = orderClient;
		this.userClient = userClient;
		this.cartClient = cartClient;
	}

	@CircuitBreaker(name = "order")
	public Mono<OrderResponse> placeOrderWithLogin(ShopUser shopUser, Cart cart, Order order) {
		final String customerId = shopUser.getSubject();
		final String accessToken = shopUser.getAccessToken().getTokenValue();
		return Mono.zip(this.createAddress(order, accessToken), this.createCard(order, accessToken))
				.flatMap(tpl -> this.cartClient.merge(customerId, cart)
						.then(this.orderClient.createOrder(tpl.getT1(), tpl.getT2(), accessToken)));
	}

	@CircuitBreaker(name = "order")
	public Mono<OrderResponse> placeOrderWithoutLogin(Cart cart, Order order, OAuth2AuthorizedClient authorizedClient) {
		final ClientRegistration client = authorizedClient.getClientRegistration();
		final String username = order.isCreateAccount() ? order.getUsername() : UUID.randomUUID().toString();
		final String password = order.isCreateAccount() ? order.getPassword() : UUID.randomUUID().toString();
		final CustomerCreateRequest customerCreateRequest = new CustomerCreateRequest()
				.username(username)
				.password(password)
				.firstName(order.getFirstName())
				.lastName(order.getLastName())
				.email(order.getEmail())
				.allowDuplicateEmail(!order.isCreateAccount());
		return this.userClient.createUser(customerCreateRequest)
				.then(this.userClient.retrieveToken(username, password, client.getClientId(), client.getClientSecret())
						.flatMap(accessToken -> {
							final String customerId = customerId(accessToken);
							return Mono.zip(this.createAddress(order, accessToken), this.createCard(order, accessToken))
									.flatMap(tpl -> this.cartClient.merge(customerId, cart)
											.then(this.orderClient.createOrder(tpl.getT1(), tpl.getT2(), accessToken)));
						}));
	}

	Mono<String> createAddress(Order order, String accessToken) {
		return this.userClient.createAddress(new CustomerAddressCreateRequest()
						.number(order.getNumber())
						.street(order.getStreet())
						.city(order.getCity())
						.postcode(order.getPostcode())
						.country(order.getCountry())
				, accessToken);
	}

	Mono<String> createCard(Order order, String accessToken) {
		return this.userClient.createCard(new CustomerCardCreateRequest()
						.longNum(order.getLongNum())
						.expires(order.parseExpires())
						.ccv(order.getCcv())
				, accessToken);
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
