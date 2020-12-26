package lol.maki.socks.user;

import lol.maki.socks.config.LoggingExchangeFilterFunction;
import lol.maki.socks.config.SockProps;
import lol.maki.socks.user.client.CustomerResponse;
import reactor.core.publisher.Mono;

import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class UserClient {
	private final WebClient webClient;

	private final SockProps props;

	public UserClient(WebClient.Builder builder, ReactiveOAuth2AuthorizedClientManager authorizedClientManager, SockProps props) {
		this.webClient = builder
				.filter(new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager))
				.filter(LoggingExchangeFilterFunction.SINGLETON)
				.build();
		this.props = props;
	}

	public Mono<CustomerResponse> getMe(String token) {
		return this.webClient.get()
				.uri(props.getUserUrl(), b -> b.path("me").build())
				.headers(httpHeaders -> httpHeaders.setBearerAuth(token))
				.retrieve()
				.bodyToMono(CustomerResponse.class);
	}
}
