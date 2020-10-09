package lol.maki.socks.cart;

import java.time.Duration;
import java.util.UUID;

import lol.maki.socks.cart.client.CartResponse;
import lol.maki.socks.config.SockProps;
import reactor.core.publisher.Mono;

import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.IdGenerator;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolverSupport;
import org.springframework.web.server.ServerWebExchange;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Component
public class CartHandlerMethodArgumentResolver extends HandlerMethodArgumentResolverSupport {
	public static String CART_ID_COOKIE_NAME = "X-CartId-Id";

	private final WebClient webClient;

	private final IdGenerator idGenerator = new AlternativeJdkIdGenerator();

	protected CartHandlerMethodArgumentResolver(ReactiveAdapterRegistry adapterRegistry, Builder builder, ReactiveOAuth2AuthorizedClientManager authorizedClientManager, SockProps props) {
		super(adapterRegistry);
		this.webClient = builder.filter(new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager))
				.baseUrl(props.getCartUrl())
				.build();
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return checkParameterType(parameter, Cart.class::isAssignableFrom);
	}

	@Override
	public Mono<Object> resolveArgument(MethodParameter methodParameter, BindingContext bindingContext, ServerWebExchange exchange) {
		final MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();
		final String cartId;
		if (cookies.containsKey(CART_ID_COOKIE_NAME)) {
			cartId = cookies.getFirst(CART_ID_COOKIE_NAME).getValue();
		}
		else {
			cartId = Cart.generateSessionId(this.idGenerator::generateId);
			// TODO or customerId from session
			final ResponseCookie cookie = ResponseCookie.from(CART_ID_COOKIE_NAME, cartId)
					.maxAge(Duration.ofDays(3))
					.httpOnly(true)
					.path("/")
					.build();
			exchange.getResponse().addCookie(cookie);
		}
		return this.webClient.get()
				.uri("carts/{cartId}", cartId)
				.attributes(clientRegistrationId("sock"))
				.retrieve()
				.bodyToMono(CartResponse.class)
				.map(c -> new Cart(cartId, c.getItems().stream()
						.map(i -> new CartItem(UUID.fromString(i.getItemId()), i.getQuantity(), i.getUnitPrice()))
						.collect(toUnmodifiableList())))
				.doOnNext(cart -> bindingContext.getModel()
						.addAttribute("cart", cart)
						.addAttribute("itemSize", cart.getItemSize())
						.addAttribute("total", cart.getTotal()))
				.cast(Object.class);
	}
}
