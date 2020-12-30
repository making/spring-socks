package lol.maki.socks.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lol.maki.socks.catalog.client.SockResponse;
import lol.maki.socks.catalog.client.TagsResponse;
import lol.maki.socks.config.LoggingExchangeFilterFunction;
import lol.maki.socks.config.SockProps;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Component
public class CatalogClient {
	private final WebClient webClient;

	private final SockProps props;

	private static final SockResponse fallbackSock = new SockResponse()
			.name("Sorry Sock")
			.description("Sorry, the service temporarily unavailable")
			.imageUrl(List.of("/img/onegai_gomen_man.png", "/img/onegai_gomen_woman.png"))
			.price(BigDecimal.ZERO)
			.tag(List.of("sorry"))
			.id(UUID.fromString("00000000-0000-0000-0000-000000000000"))
			.count(0);

	private static final Resource fallbackImage = new ClassPathResource("static/img/spring_socks_1.jpg");

	public CatalogClient(WebClient.Builder builder, LoadBalancedExchangeFilterFunction loadBalancerExchangeFilterFunction, ReactiveOAuth2AuthorizedClientManager authorizedClientManager, SockProps props) {
		this.webClient = builder
				.filter(new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager))
				.filter(loadBalancerExchangeFilterFunction)
				.filter(LoggingExchangeFilterFunction.SINGLETON)
				.build();
		this.props = props;
	}

	@CircuitBreaker(name = "catalog")
	public Mono<SockResponse> getSock(UUID id) {
		return this.webClient.get()
				.uri(props.getCatalogUrl(), b -> b.path("catalogue/{id}").build(id))
				.attributes(clientRegistrationId("sock"))
				.retrieve()
				.bodyToMono(SockResponse.class);
	}

	@CircuitBreaker(name = "catalog", fallbackMethod = "fallbackSock")
	public Mono<SockResponse> getSockWithFallback(UUID id) {
		return this.getSock(id);
	}

	@CircuitBreaker(name = "catalog")
	public Flux<SockResponse> getSocks(CatalogOrder order, int page, int size, List<String> tags) {
		return this.webClient.get()
				.uri(props.getCatalogUrl(), b -> b.path("catalogue")
						.queryParam("order", order)
						.queryParam("page", page)
						.queryParam("size", size)
						.queryParam("tags", tags)
						.build())
				.attributes(clientRegistrationId("sock"))
				.retrieve()
				.bodyToFlux(SockResponse.class);
	}

	@CircuitBreaker(name = "catalog", fallbackMethod = "fallbackSocks")
	public Flux<SockResponse> getSocksWithFallback(CatalogOrder order, int page, int size, List<String> tags) {
		return this.getSocks(order, page, size, tags);
	}

	@CircuitBreaker(name = "catalog")
	public Mono<TagsResponse> getTags() {
		return this.webClient.get()
				.uri(props.getCatalogUrl(), b -> b.path("tags").build())
				.attributes(clientRegistrationId("sock"))
				.retrieve()
				.bodyToMono(TagsResponse.class);
	}

	@CircuitBreaker(name = "catalog", fallbackMethod = "fallbackTags")
	public Mono<TagsResponse> getTagsWithFallback() {
		return this.getTags();
	}

	@CircuitBreaker(name = "catalog", fallbackMethod = "fallbackImage")
	public Mono<Resource> getImageWithFallback(String fileName) {
		return this.webClient.get()
				.uri(props.getCatalogUrl(), b -> b.path("images/{fileName}").build(fileName))
				.retrieve()
				.bodyToMono(Resource.class);
	}

	@CircuitBreaker(name = "catalog", fallbackMethod = "fallbackImage")
	public Mono<Resource> headImageWithFallback(String fileName) {
		return this.webClient.head()
				.uri(props.getCatalogUrl(), b -> b.path("images/{fileName}").build(fileName))
				.retrieve()
				.bodyToMono(Resource.class);
	}

	Mono<SockResponse> fallbackSock(UUID id, Throwable throwable) {
		SockNotFoundException.throwIfNotFound(id, throwable);
		return Mono.just(fallbackSock);
	}

	Flux<SockResponse> fallbackSocks(CatalogOrder order, int page, int size, List<String> tags, Throwable throwable) {
		return Flux.just(fallbackSock);
	}

	Mono<TagsResponse> fallbackTags(Throwable throwable) {
		return Mono.fromCallable(TagsResponse::new);
	}

	Mono<Resource> fallbackImage(String fileName, Throwable throwable) {
		return Mono.just(fallbackImage);
	}
}
