package lol.maki.socks.catalog.web;

import java.util.List;

import lol.maki.socks.cart.Cart;
import lol.maki.socks.catalog.CatalogClient;
import lol.maki.socks.catalog.CatalogOrder;
import lol.maki.socks.catalog.client.SockResponse;
import lol.maki.socks.catalog.client.TagsResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
	private final CatalogClient catalogClient;

	public HomeController(CatalogClient catalogClient) {
		this.catalogClient = catalogClient;
	}

	@GetMapping(path = "/")
	public String home(Model model, Cart cart, @RegisteredOAuth2AuthorizedClient("sock") OAuth2AuthorizedClient authorizedClient) {
		final Flux<SockResponse> socks = this.catalogClient.getSocksWithFallback(CatalogOrder.PRICE, 1, 6, List.of("featured"), authorizedClient);
		final Mono<TagsResponse> tags = this.catalogClient.getTagsWithFallback(authorizedClient);
		model.addAttribute("socks", socks);
		model.addAttribute("tags", tags);
		return "index";
	}
}
