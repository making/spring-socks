package lol.maki.socks.cart.web;

import java.util.Map;
import java.util.UUID;

import lol.maki.socks.cart.Cart;
import lol.maki.socks.cart.client.CartItemRequest;
import lol.maki.socks.cart.client.CartItemResponse;
import lol.maki.socks.catalog.CatalogClient;
import lol.maki.socks.config.SockProps;
import reactor.core.publisher.Mono;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@Controller
public class CartController {
	private final CatalogClient catalogClient;

	private final WebClient webClient;

	private final SockProps props;

	public CartController(CatalogClient catalogClient, Builder builder, ReactiveOAuth2AuthorizedClientManager authorizedClientManager, SockProps props) {
		this.catalogClient = catalogClient;
		this.webClient = builder
				.filter(new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager))
				.build();
		this.props = props;
	}

	@PostMapping(path = "cart")
	public Mono<String> addCart(@ModelAttribute AddCartItemForm cartItem, Cart cart, @RegisteredOAuth2AuthorizedClient("sock") OAuth2AuthorizedClient authorizedClient) {
		return this.catalogClient.getSock(cartItem.getId(), authorizedClient)
				.map(item -> new CartItemRequest()
						.itemId(item.getId().toString())
						.quantity(cartItem.getQuantity())
						.unitPrice(item.getPrice()))
				.flatMap(item -> this.webClient.post()
						.uri(this.props.getCartUrl(), b -> b.path("carts/{cartId}/items").build(cart.getCartId()))
						.attributes(oauth2AuthorizedClient(authorizedClient))
						.bodyValue(item)
						.retrieve()
						.bodyToMono(CartItemResponse.class))
				.thenReturn("redirect:/cart");
	}

	@PostMapping(path = "cart", params = "delete")
	public Mono<String> deleteCartItem(@ModelAttribute DeleteCartItemForm cartItem, Cart cart, @RegisteredOAuth2AuthorizedClient("sock") OAuth2AuthorizedClient authorizedClient) {
		return this.webClient.delete()
				.uri(this.props.getCartUrl(), b -> b.path("carts/{cartId}/items/{itemId}").build(cart.getCartId(), cartItem.getId().toString()))
				.attributes(oauth2AuthorizedClient(authorizedClient))
				.retrieve()
				.toBodilessEntity()
				.thenReturn("redirect:/cart");
	}

	@PostMapping(path = "cart", params = "update")
	public Mono<String> updateCart(@ModelAttribute UpdateCartForm cartForm, Cart cart, @RegisteredOAuth2AuthorizedClient("sock") OAuth2AuthorizedClient authorizedClient) {
		final Mono<Cart> latestCart = cart.retrieveLatest(props.getCatalogUrl(), this.webClient, authorizedClient);
		final Map<UUID, Integer> cartItems = cartForm.getCartItems();
		return latestCart.flatMapIterable(Cart::getItems)
				.filter(item -> cartItems.containsKey(item.getItemId()))
				.flatMap(item -> {
					final Integer requested = cartItems.get(item.getItemId());
					if (requested == 0) {
						// DELETE
						return this.webClient.delete()
								.uri(this.props.getCartUrl(), b -> b.path("carts/{cartId}/items/{itemId}").build(cart.getCartId(), item.getItemId().toString()))
								.attributes(oauth2AuthorizedClient(authorizedClient))
								.retrieve()
								.toBodilessEntity();
					}
					else if (!requested.equals(item.getQuantity())) {
						// UPDATE
						final CartItemRequest cartItemRequest = new CartItemRequest()
								.itemId(item.getItemId().toString())
								.unitPrice(item.getUnitPrice())
								.quantity(requested);
						return this.webClient.patch()
								.uri(this.props.getCartUrl(), b -> b.path("carts/{cartId}/items").build(cart.getCartId()))
								.attributes(oauth2AuthorizedClient(authorizedClient))
								.bodyValue(cartItemRequest)
								.retrieve()
								.toBodilessEntity();
					}
					return Mono.empty();
				})
				.collectList()
				.thenReturn("redirect:/cart");
	}

	@PostMapping(path = "cart", params = "coupon")
	public Mono<String> applyCoupon() {
		return Mono.just("redirect:/cart");
	}

	@GetMapping(path = "cart")
	public Mono<String> viewCart(Cart cart, Model model, @RegisteredOAuth2AuthorizedClient("sock") OAuth2AuthorizedClient authorizedClient) {
		final Mono<Cart> latestCart = cart.retrieveLatest(props.getCatalogUrl(), this.webClient, authorizedClient);
		model.addAttribute("cart", latestCart);
		return Mono.just("shopping-cart");
	}


	public static class AddCartItemForm {
		private UUID id;

		private Integer quantity;

		public UUID getId() {
			return id;
		}

		public void setId(UUID id) {
			this.id = id;
		}

		public Integer getQuantity() {
			return quantity;
		}

		public void setQuantity(Integer quantity) {
			this.quantity = quantity;
		}
	}

	public static class DeleteCartItemForm {
		private UUID id;

		public UUID getId() {
			return id;
		}

		public void setId(UUID id) {
			this.id = id;
		}
	}

	public static class UpdateCartForm {
		private Map<UUID, Integer> cartItems;

		public Map<UUID, Integer> getCartItems() {
			return cartItems;
		}

		public void setCartItems(Map<UUID, Integer> cartItems) {
			this.cartItems = cartItems;
		}

		@Override
		public String toString() {
			return "UpdateCartForm{" +
					"cartItems=" + cartItems +
					'}';
		}
	}
}
