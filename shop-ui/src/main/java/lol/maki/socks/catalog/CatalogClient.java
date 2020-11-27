package lol.maki.socks.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import lol.maki.socks.catalog.client.SockResponse;
import lol.maki.socks.catalog.client.TagsResponse;
import lol.maki.socks.config.SockProps;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

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

	public CatalogClient(WebClient.Builder builder, ReactiveOAuth2AuthorizedClientManager authorizedClientManager, SockProps props) {
		this.webClient = builder
				.filter(new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager))
				.build();
		this.props = props;
	}

	public Mono<SockResponse> getSock(UUID id, OAuth2AuthorizedClient authorizedClient) {
		return this.webClient.get()
				.uri(props.getCatalogUrl(), b -> b.path("catalogue/{id}").build(id))
				.attributes(oauth2AuthorizedClient(authorizedClient))
				.retrieve()
				.bodyToMono(SockResponse.class);
	}

	public Mono<SockResponse> getSockWithFallback(UUID id, OAuth2AuthorizedClient authorizedClient) {
		return this.getSock(id, authorizedClient)
				.onErrorReturn(fallbackSock);
	}

	public Flux<SockResponse> getSocksWithFallback(int page, int size, List<String> tags, OAuth2AuthorizedClient authorizedClient) {
		return this.webClient.get()
				.uri(props.getCatalogUrl(), b -> b.path("catalogue")
						.queryParam("page", page)
						.queryParam("size", size)
						.queryParam("tags", tags)
						.build())
				.attributes(oauth2AuthorizedClient(authorizedClient))
				.retrieve()
				.bodyToFlux(SockResponse.class)
				.onErrorReturn(fallbackSock);
	}

	public Mono<TagsResponse> getTagsWithFallback(OAuth2AuthorizedClient authorizedClient) {
		return this.webClient.get()
				.uri(props.getCatalogUrl(), b -> b.path("tags").build())
				.attributes(oauth2AuthorizedClient(authorizedClient))
				.retrieve()
				.bodyToMono(TagsResponse.class)
				.onErrorReturn(new TagsResponse());
	}

	public Mono<ResponseEntity<Resource>> getImageWithFallback(String fileName) {
		return this.webClient.get()
				.uri(props.getCatalogUrl(), b -> b.path("images/{fileName}").build(fileName))
				.retrieve()
				.toEntity(Resource.class)
				.onErrorReturn(ResponseEntity.ok(fallbackImage));
	}

	public Mono<ResponseEntity<Resource>> headImageWithFallback(String fileName) {
		return this.webClient.head()
				.uri(props.getCatalogUrl(), b -> b.path("images/{fileName}").build(fileName))
				.retrieve()
				.toEntity(Resource.class)
				.onErrorReturn(ResponseEntity.ok(fallbackImage));
	}
}
