package lol.maki.socks.shop;

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
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@Controller
public class HomeController {
	private final WebClient webClient;

	private final SockProps props;

	public HomeController(WebClient.Builder builder, ReactiveOAuth2AuthorizedClientManager authorizedClientManager, SockProps props) {
		this.webClient = builder
				.filter(new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager))
				.build();
		this.props = props;
	}

	@GetMapping(path = "/")
	public String home(Model model, Cart cart, @RegisteredOAuth2AuthorizedClient("sock") OAuth2AuthorizedClient authorizedClient) {
		final Flux<SockResponse> socks = this.webClient.get()
				.uri(props.getCatalogUrl(), b -> b.path("catalogue")
						.queryParam("page", 1)
						.queryParam("size", 6)
						.build())
				.attributes(oauth2AuthorizedClient(authorizedClient))
				.retrieve()
				.bodyToFlux(SockResponse.class);
		final Mono<TagsResponse> tags = this.webClient.get()
				.uri(props.getCatalogUrl(), b -> b.path("tags").build())
				.attributes(oauth2AuthorizedClient(authorizedClient))
				.retrieve()
				.bodyToMono(TagsResponse.class); model.addAttribute("socks", socks);
		model.addAttribute("socks", socks);
		model.addAttribute("tags", tags);
		return "index";
	}
}
