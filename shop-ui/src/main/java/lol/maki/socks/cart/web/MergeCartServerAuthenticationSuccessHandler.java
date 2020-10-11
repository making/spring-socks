package lol.maki.socks.cart.web;

import lol.maki.socks.config.SockProps;
import lol.maki.socks.security.ShopUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import static lol.maki.socks.cart.web.CartHandlerMethodArgumentResolver.CART_ID_COOKIE_NAME;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;


public class MergeCartServerAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {
	private final Logger log = LoggerFactory.getLogger(MergeCartServerAuthenticationSuccessHandler.class);

	private final WebClient webClient;

	public MergeCartServerAuthenticationSuccessHandler(Builder builder, ReactiveOAuth2AuthorizedClientManager authorizedClientManager, SockProps props) {
		this.webClient = builder.filter(new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager))
				.baseUrl(props.getCartUrl())
				.build();
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
		return this.webClient.get()
				.uri("carts/{customerId}/merge?sessionId={sessionId}", customerId, sessionId)
				.attributes(clientRegistrationId("sock"))
				.retrieve()
				.toBodilessEntity()
				.then();
	}
}
