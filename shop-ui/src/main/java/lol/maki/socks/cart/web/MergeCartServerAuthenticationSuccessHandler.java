package lol.maki.socks.cart.web;

import lol.maki.socks.cart.CartClient;
import lol.maki.socks.security.ShopUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.util.MultiValueMap;

import static lol.maki.socks.cart.web.CartHandlerMethodArgumentResolver.CART_ID_COOKIE_NAME;

public class MergeCartServerAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {
	private final Logger log = LoggerFactory.getLogger(MergeCartServerAuthenticationSuccessHandler.class);

	private final CartClient cartClient;

	public MergeCartServerAuthenticationSuccessHandler(CartClient cartClient) {
		this.cartClient = cartClient;
	}

	@Override
	public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
		final MultiValueMap<String, HttpCookie> cookies = webFilterExchange.getExchange().getRequest().getCookies();
		if (!cookies.containsKey(CART_ID_COOKIE_NAME)) {
			return Mono.empty();
		}
		final String sessionId = cookies.getFirst(CART_ID_COOKIE_NAME).getValue();
		final String customerId = ((ShopUser) authentication.getPrincipal()).customerId();
		log.info("Merge cart from {} to {}", sessionId, customerId);
		return this.cartClient.mergeWithFallback(customerId, sessionId);
	}
}
