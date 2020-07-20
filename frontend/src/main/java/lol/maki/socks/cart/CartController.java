package lol.maki.socks.cart;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import lol.maki.socks.LoggedInUser;
import lol.maki.socks.cart.client.CartApi;
import lol.maki.socks.cart.client.CartItemRequest;
import lol.maki.socks.cart.client.CartItemResponse;
import lol.maki.socks.catalog.client.CatalogApi;
import lol.maki.socks.catalog.client.SockResponse;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CartController {
	private final CatalogApi catalogApi;

	private final CartApi cartApi;

	public CartController(CatalogApi catalogApi, CartApi cartApi) {
		this.catalogApi = catalogApi;
		this.cartApi = cartApi;
	}

	@GetMapping(path = "cart")
	public Mono<List<CartItemResponse>> listItemsInCart() {
		final String customerId = LoggedInUser.customerId();
		return this.cartApi.getItemsByCustomerId(customerId).collectList();
	}

	@DeleteMapping(path = "cart")
	public Mono<Void> deleteCart() {
		final String customerId = LoggedInUser.customerId();
		return this.cartApi.deleteCartByCustomerId(customerId);
	}

	@DeleteMapping(path = "cart/{id}")
	public Mono<Void> deleteItemFromCart(@PathVariable("id") String itemId) {
		final String customerId = LoggedInUser.customerId();
		return this.cartApi.deleteCartItemByCartIdAndItemId(customerId, itemId);
	}

	@PostMapping(path = "cart")
	public Mono<CartItemResponse> addNewITemToCart(@RequestBody JsonNode req) {
		final String customerId = LoggedInUser.customerId();
		final String itemId = req.get("id").asText();
		final Mono<SockResponse> sockMono = this.catalogApi.getSock(UUID.fromString(itemId));
		return sockMono
				.map(sock -> new CartItemRequest().itemId(itemId).unitPrice(sock.getPrice()).quantity(1))
				.flatMap(cartItem -> this.cartApi.postItemsByCustomerId(customerId, cartItem));
	}

	@PostMapping(path = "cart/update")
	public Mono<Void> updateCartItem(@RequestBody JsonNode req) {
		final String customerId = LoggedInUser.customerId();
		final String itemId = req.get("id").asText();
		final int quantity = req.get("quantity").asInt();
		final Mono<SockResponse> sockMono = this.catalogApi.getSock(UUID.fromString(itemId));
		return sockMono
				.map(sock -> new CartItemRequest().itemId(itemId).unitPrice(sock.getPrice()).quantity(quantity))
				.flatMap(cartItem -> this.cartApi.patchItemsByCustomerId(customerId, cartItem));
	}
}
