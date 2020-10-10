package lol.maki.socks.security;

import java.net.URI;
import java.util.Map;

import reactor.core.publisher.Mono;

import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.web.server.ServerWebExchange;

public class RedirectToServerRedirectStrategy implements ServerRedirectStrategy {
	private final ServerRedirectStrategy delegate = new DefaultServerRedirectStrategy();

	public static final String REDIRECT_TO_ATTR = "redirectTo";

	@Override
	public Mono<Void> sendRedirect(ServerWebExchange exchange, URI location) {
		return exchange.getSession()
				.flatMap(webSession -> {
					final Map<String, Object> attributes = webSession.getAttributes();
					if (attributes.containsKey(REDIRECT_TO_ATTR)) {
						final URI redirectTo = (URI) attributes.get(REDIRECT_TO_ATTR);
						attributes.remove(REDIRECT_TO_ATTR);
						return this.delegate.sendRedirect(exchange, redirectTo);
					}
					return this.delegate.sendRedirect(exchange, location);
				});
	}
}
