package lol.maki.socks.catalog.web;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import lol.maki.socks.cart.Cart;
import lol.maki.socks.catalog.CatalogClient;
import lol.maki.socks.catalog.CatalogOrder;
import lol.maki.socks.catalog.SockNotFoundException;
import lol.maki.socks.catalog.client.SockResponse;
import lol.maki.socks.catalog.client.TagsResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.result.view.Rendering;

import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

@Controller
public class CatalogController {
	private final CatalogClient catalogClient;

	public CatalogController(CatalogClient catalogClient) {
		this.catalogClient = catalogClient;
	}

	@GetMapping(path = "details/{id}")
	public String details(@PathVariable("id") UUID id, Model model, Cart cart) {
		final Mono<SockResponse> sock = this.catalogClient.getSockWithFallback(id);
		final Flux<SockResponse> relatedProducts = sock.flatMapMany(s -> this.catalogClient.getSocksWithFallback(CatalogOrder.PRICE, 1, 4, s.getTag()));
		final Mono<TagsResponse> tags = this.catalogClient.getTagsWithFallback();
		model.addAttribute("sock", sock);
		model.addAttribute("relatedProducts", relatedProducts);
		model.addAttribute("tags", tags);
		return "shop-details";
	}

	@ResponseBody
	@GetMapping(path = "details/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<Map<String, ?>> details(@PathVariable("id") UUID id) {
		return this.catalogClient.getSockWithFallback(id)
				.flatMap(sock -> this.catalogClient.getSocksWithFallback(CatalogOrder.PRICE, 1, 4, sock.getTag())
						.filter(s -> !Objects.equals(s.getId(), sock.getId()))
						.collectList()
						.map(relatedProducts -> Map.of("sock", sock, "relatedProducts", relatedProducts)));
	}

	@GetMapping(path = "tags/{tag}")
	public String tag(@PathVariable("tag") List<String> tag, @RequestParam(name = "order", defaultValue = "price") CatalogOrder order, Model model, Cart cart) {
		final Flux<SockResponse> socks = this.catalogClient.getSocksWithFallback(order, 1, 10, tag);
		final Mono<TagsResponse> tags = this.catalogClient.getTagsWithFallback();
		model.addAttribute("socks", socks);
		model.addAttribute("tags", tags);
		model.addAttribute("order", order.toString());
		return "shop-grid";
	}

	@ResponseBody
	@GetMapping(path = "tags", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<TagsResponse> tags() {
		return this.catalogClient.getTagsWithFallback();
	}

	@ResponseBody
	@GetMapping(path = "tags/{tag}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Flux<SockResponse> tag(@PathVariable("tag") List<String> tag,
			@RequestParam(name = "order", defaultValue = "price") CatalogOrder order,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "size", defaultValue = "10") int size) {
		return this.catalogClient.getSocksWithFallback(order, page, size, tag);
	}

	@ResponseBody
	@GetMapping(path = "images/{fileName:.+}")
	public Mono<ResponseEntity<Resource>> getImage(@PathVariable String fileName) {
		Mono<Resource> image = fileName.startsWith("img/") ? Mono.just(new ClassPathResource(fileName)) : this.catalogClient.getImageWithFallback(fileName);
		return image.map(body -> ResponseEntity.ok()
				.cacheControl(CacheControl.maxAge(Duration.ofDays(7)))
				.body(body));
	}

	@ResponseBody
	@RequestMapping(method = HEAD, path = "images/{fileName:.+}")
	public Mono<ResponseEntity<Resource>> headImage(@PathVariable String fileName) {
		return this.catalogClient.headImageWithFallback(fileName)
				.map(body -> ResponseEntity.ok()
						.cacheControl(CacheControl.maxAge(Duration.ofDays(7)))
						.body(body));
	}

	@ExceptionHandler(SockNotFoundException.class)
	public Rendering sockNotFound(SockNotFoundException e) {
		return Rendering
				.view("shop-details")
				.status(HttpStatus.NOT_FOUND)
				.modelAttribute("sock", e.notFound())
				.modelAttribute("relatedProducts", List.of())
				.modelAttribute("tags", new TagsResponse())
				.build();
	}
}
