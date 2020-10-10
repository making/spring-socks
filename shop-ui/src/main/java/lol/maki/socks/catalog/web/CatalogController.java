package lol.maki.socks.catalog.web;

import java.util.List;
import java.util.UUID;

import lol.maki.socks.cart.Cart;
import lol.maki.socks.catalog.client.SockResponse;
import lol.maki.socks.catalog.client.TagsResponse;
import lol.maki.socks.config.SockProps;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@Controller
public class CatalogController {
	private final WebClient webClient;

	private final SockProps props;

	public CatalogController(WebClient.Builder builder, ReactiveOAuth2AuthorizedClientManager authorizedClientManager, SockProps props) {
		this.webClient = builder
				.filter(new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager))
				.build();
		this.props = props;
	}

	@GetMapping(path = "details/{id}")
	public String details(@PathVariable("id") UUID id, Model model, Cart cart, @RegisteredOAuth2AuthorizedClient("sock") OAuth2AuthorizedClient authorizedClient) {
		final Mono<SockResponse> sock = this.webClient.get()
				.uri(props.getCatalogUrl(), b -> b.path("catalogue/{id}").build(id))
				.attributes(oauth2AuthorizedClient(authorizedClient))
				.retrieve()
				.bodyToMono(SockResponse.class);
		final Flux<SockResponse> relatedProducts = sock
				.flatMapMany(s -> this.webClient.get()
						.uri(props.getCatalogUrl(), b -> b.path("catalogue")
								.queryParam("page", 1)
								.queryParam("size", 4)
								.queryParam("tags", s.getTag())
								.build())
						.attributes(oauth2AuthorizedClient(authorizedClient))
						.retrieve()
						.bodyToFlux(SockResponse.class));
		final Mono<TagsResponse> tags = this.webClient.get()
				.uri(props.getCatalogUrl(), b -> b.path("tags").build())
				.attributes(oauth2AuthorizedClient(authorizedClient))
				.retrieve()
				.bodyToMono(TagsResponse.class);
		model.addAttribute("sock", sock);
		model.addAttribute("relatedProducts", relatedProducts);
		model.addAttribute("tags", tags);
		return "shop-details";
	}

	@GetMapping(path = "tags/{tag}")
	public String tag(@PathVariable("tag") List<String> tag, Model model, Cart cart, @RegisteredOAuth2AuthorizedClient("sock") OAuth2AuthorizedClient authorizedClient) {
		final Flux<SockResponse> socks = this.webClient.get()
				.uri(props.getCatalogUrl(), b -> b.path("catalogue")
						.queryParam("page", 1)
						.queryParam("size", 10)
						.queryParam("tags", tag)
						.build())
				.attributes(oauth2AuthorizedClient(authorizedClient))
				.retrieve()
				.bodyToFlux(SockResponse.class);
		final Mono<TagsResponse> tags = this.webClient.get()
				.uri(props.getCatalogUrl(), b -> b.path("tags").build())
				.attributes(oauth2AuthorizedClient(authorizedClient))
				.retrieve()
				.bodyToMono(TagsResponse.class); model.addAttribute("socks", socks);
		model.addAttribute("tags", tags);
		return "shop-grid";
	}
}
