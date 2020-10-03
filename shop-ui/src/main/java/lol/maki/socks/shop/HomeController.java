package lol.maki.socks.shop;

import java.util.List;

import lol.maki.socks.cart.Cart;
import lol.maki.socks.catalog.client.CatalogApi;
import lol.maki.socks.catalog.client.SockResponse;
import lol.maki.socks.catalog.client.TagsResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
	private final CatalogApi catalogApi;

	public HomeController(CatalogApi catalogApi) {
		this.catalogApi = catalogApi;
	}

	@GetMapping(path = "/")
	public String home(Model model, Cart cart) {
		final Flux<SockResponse> socks = this.catalogApi.getSocks(null, 1, 6, List.of());
		final Mono<TagsResponse> tags = this.catalogApi.getTags();
		model.addAttribute("socks", socks);
		model.addAttribute("tags", tags);
		return "index";
	}
}
