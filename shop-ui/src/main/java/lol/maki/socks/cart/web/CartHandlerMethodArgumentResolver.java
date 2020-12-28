package lol.maki.socks.cart.web;

import java.time.Duration;

import lol.maki.socks.cart.Cart;
import lol.maki.socks.cart.CartClient;
import lol.maki.socks.security.ShopUser;
import reactor.core.publisher.Mono;

import org.springframework.context.annotation.Lazy;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.IdGenerator;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolverSupport;
import org.springframework.web.server.ServerWebExchange;

@Component
public class CartHandlerMethodArgumentResolver extends HandlerMethodArgumentResolverSupport {
	public static String CART_ID_COOKIE_NAME = "X-CartId-Id";

	private final CartClient cartClient;

	private final IdGenerator idGenerator = new AlternativeJdkIdGenerator();

	protected CartHandlerMethodArgumentResolver(@Lazy ReactiveAdapterRegistry adapterRegistry, CartClient cartClient) {
		super(adapterRegistry);
		this.cartClient = cartClient;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return checkParameterType(parameter, Cart.class::isAssignableFrom);
	}

	Mono<String> cookieCartId(ServerWebExchange exchange) {
		return Mono.fromCallable(() -> {
			final MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();
			final String cartId;
			if (cookies.containsKey(CART_ID_COOKIE_NAME)) {
				cartId = cookies.getFirst(CART_ID_COOKIE_NAME).getValue();
			}
			else {
				cartId = Cart.generateSessionId(this.idGenerator::generateId);
				final ResponseCookie cookie = ResponseCookie.from(CART_ID_COOKIE_NAME, cartId)
						.maxAge(Duration.ofDays(3))
						.httpOnly(true)
						.path("/")
						.build();
				exchange.getResponse().addCookie(cookie);
			}
			return cartId;
		});
	}

	Mono<String> cartId(ServerWebExchange exchange) {
		return ReactiveSecurityContextHolder.getContext()
				.map(SecurityContext::getAuthentication)
				.map(Authentication::getPrincipal)
				.cast(ShopUser.class)
				.map(ShopUser::customerId)
				.switchIfEmpty(this.cookieCartId(exchange));
	}

	@Override
	public Mono<Object> resolveArgument(MethodParameter methodParameter, BindingContext bindingContext, ServerWebExchange exchange) {
		return this.cartId(exchange)
				.flatMap(cartId -> this.cartClient.findOneWithFallback(cartId)
						.doOnNext(cart -> bindingContext.getModel()
								.addAttribute("cart", cart)
								.addAttribute("itemSize", cart.getItemSize())
								.addAttribute("total", cart.getTotal()))
						.cast(Object.class));
	}
}
