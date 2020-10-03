package lol.maki.socks.shop;

import java.util.List;
import java.util.UUID;

import lol.maki.socks.cart.Cart;
import lol.maki.socks.catalog.client.CatalogApi;
import lol.maki.socks.catalog.client.SockResponse;
import lol.maki.socks.catalog.client.TagsResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CatalogController {
	private final CatalogApi catalogApi;

	public CatalogController(CatalogApi catalogApi) {
		this.catalogApi = catalogApi;
	}

	@GetMapping(path = "details/{id}")
	public String details(@PathVariable("id") UUID id, Model model, Cart cart) {
		final Mono<SockResponse> sock = this.catalogApi.getSock(id);
		final Flux<SockResponse> relatedProducts = sock
				.flatMapMany(s -> this.catalogApi.getSocks(null, 1, 4, s.getTag()));
		final Mono<TagsResponse> tags = this.catalogApi.getTags();
		model.addAttribute("sock", sock);
		model.addAttribute("relatedProducts", relatedProducts);
		model.addAttribute("tags", tags);
		return "shop-details";
	}

	@GetMapping(path = "tags/{tag}")
	public String tag(@PathVariable("tag") List<String> tag, Model model, Cart cart) {
		final Flux<SockResponse> socks = this.catalogApi.getSocks(null, 1, 10, tag);
		final Mono<TagsResponse> tags = this.catalogApi.getTags();
		model.addAttribute("socks", socks);
		model.addAttribute("tags", tags);
		return "shop-grid";
	}
}
