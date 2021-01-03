package lol.maki.socks.order;

import java.io.IOException;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import lol.maki.socks.config.LoggingExchangeFilterFunction;
import lol.maki.socks.config.SockProps;
import lol.maki.socks.order.client.OrderRequest;
import lol.maki.socks.order.client.OrderResponse;
import lol.maki.socks.payment.client.AuthorizationResponse;
import reactor.core.publisher.Mono;

import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static org.springframework.http.HttpStatus.CONFLICT;

@Component
public class OrderClient {

	private final WebClient webClient;

	private final SockProps props;

	private final ObjectMapper objectMapper;

	public OrderClient(Builder builder, SockProps props, LoadBalancedExchangeFilterFunction loadBalancerExchangeFilterFunction, ReactiveOAuth2AuthorizedClientManager authorizedClientManager, ObjectMapper objectMapper) {
		this.webClient = builder
				.filter(new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager))
				.filter(loadBalancerExchangeFilterFunction)
				.filter(new LoggingExchangeFilterFunction(false))
				.build();
		this.props = props;
		this.objectMapper = objectMapper;
	}

	public Mono<OrderResponse> createOrder(String addressId, String cardId, String accessToken) {
		return this.webClient.post()
				.uri(this.props.getOrderUrl(), b -> b.path("orders").build())
				.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
				.bodyValue(new OrderRequest()
						.addressId(UUID.fromString(addressId))
						.cardId(UUID.fromString(cardId)))
				.retrieve()
				.bodyToMono(OrderResponse.class)
				.onErrorMap(WebClientResponseException.class, e -> {
					if (e.getStatusCode() == CONFLICT) {
						throw this.convertPaymentException(e);
					}
					throw e;
				});
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
