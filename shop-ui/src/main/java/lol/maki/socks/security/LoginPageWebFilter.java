package lol.maki.socks.security;

import java.net.URI;

import reactor.core.publisher.Mono;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.UriComponentsBuilder;

public class LoginPageWebFilter implements WebFilter {
	private ServerWebExchangeMatcher matcher = ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/login");

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		return this.matcher.matches(exchange)
				.filter(ServerWebExchangeMatcher.MatchResult::isMatch)
				.switchIfEmpty(chain.filter(exchange).then(Mono.empty()))
				.flatMap((matchResult) -> {
					final ServerHttpResponse response = exchange.getResponse();
					final URI location = UriComponentsBuilder.fromUri(exchange.getRequest().getURI()).replacePath("/oauth2/authorization/ui").build().toUri();
					response.getHeaders().setLocation(location);
					response.setStatusCode(HttpStatus.SEE_OTHER);
					return Mono.empty();
				});
	}
}
