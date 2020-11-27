package lol.maki.socks.catalog.web;

import java.util.List;
import java.util.UUID;

import lol.maki.socks.cart.Cart;
import lol.maki.socks.catalog.CatalogClient;
import lol.maki.socks.catalog.CatalogOrder;
import lol.maki.socks.catalog.client.SockResponse;
import lol.maki.socks.catalog.client.TagsResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

@Controller
public class CatalogController {
	private final CatalogClient catalogClient;

	public CatalogController(CatalogClient catalogClient) {
		this.catalogClient = catalogClient;
	}

	@GetMapping(path = "details/{id}")
	public String details(@PathVariable("id") UUID id, Model model, Cart cart, @RegisteredOAuth2AuthorizedClient("sock") OAuth2AuthorizedClient authorizedClient) {
		final Mono<SockResponse> sock = this.catalogClient.getSockWithFallback(id, authorizedClient);
		final Flux<SockResponse> relatedProducts = sock.flatMapMany(s -> this.catalogClient.getSocksWithFallback(CatalogOrder.PRICE, 1, 4, s.getTag(), authorizedClient));
		final Mono<TagsResponse> tags = this.catalogClient.getTagsWithFallback(authorizedClient);
		model.addAttribute("sock", sock);
		model.addAttribute("relatedProducts", relatedProducts);
		model.addAttribute("tags", tags);
		return "shop-details";
	}

	@GetMapping(path = "tags/{tag}")
	public String tag(@PathVariable("tag") List<String> tag, @RequestParam(name = "order", defaultValue = "price") CatalogOrder order, Model model, Cart cart, @RegisteredOAuth2AuthorizedClient("sock") OAuth2AuthorizedClient authorizedClient) {
		final Flux<SockResponse> socks = this.catalogClient.getSocksWithFallback(order, 1, 10, tag, authorizedClient);
		final Mono<TagsResponse> tags = this.catalogClient.getTagsWithFallback(authorizedClient);
		model.addAttribute("socks", socks);
		model.addAttribute("tags", tags);
		model.addAttribute("order", order.toString());
		return "shop-grid";
	}

	@ResponseBody
	@GetMapping(path = "images/{fileName:.+}")
	public Mono<ResponseEntity<Resource>> getImage(@PathVariable String fileName) {
		if (fileName.startsWith("img/")) {
			return Mono.just(ResponseEntity.ok(new ClassPathResource(fileName)));
		}
		return this.catalogClient.getImageWithFallback(fileName);
	}

	@ResponseBody
	@RequestMapping(method = HEAD, path = "images/{fileName:.+}")
	public Mono<ResponseEntity<Resource>> headImage(@PathVariable String fileName) {
		return this.catalogClient.headImageWithFallback(fileName);
	}
}
