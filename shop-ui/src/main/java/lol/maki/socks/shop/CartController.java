package lol.maki.socks.shop;

import java.util.Map;
import java.util.UUID;

import lol.maki.socks.cart.Cart;
import lol.maki.socks.cart.client.CartApi;
import lol.maki.socks.cart.client.CartItemRequest;
import lol.maki.socks.catalog.client.CatalogApi;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CartController {
	private final CartApi cartApi;

	private final CatalogApi catalogApi;

	public CartController(CartApi cartApi, CatalogApi catalogApi) {
		this.cartApi = cartApi;
		this.catalogApi = catalogApi;
	}

	@PostMapping(path = "cart")
	public Mono<String> addCart(@ModelAttribute AddCartItemForm cartItem, Cart cart) {
		return this.catalogApi.getSock(cartItem.getId())
				.map(item -> new CartItemRequest()
						.itemId(item.getId().toString())
						.quantity(cartItem.getQuantity())
						.unitPrice(item.getPrice()))
				.flatMap(item -> this.cartApi.postItemsByCustomerId(cart.getCartId(), item))
				.thenReturn("redirect:/cart");
	}

	@PostMapping(path = "cart", params = "delete")
	public Mono<String> deleteCartItem(@ModelAttribute DeleteCartItemForm cartItem, Cart cart) {
		return this.cartApi
				.deleteCartItemByCartIdAndItemId(cart.getCartId(), cartItem.getId().toString())
				.thenReturn("redirect:/cart");
	}

	@PostMapping(path = "cart", params = "update")
	public Mono<String> updateCart(@ModelAttribute UpdateCartForm cartForm, Cart cart) {
		final Mono<Cart> latestCart = cart.retrieveLatest(this.catalogApi);
		final Map<UUID, Integer> cartItems = cartForm.getCartItems();
		return latestCart.flatMapIterable(Cart::getItems)
				.filter(item -> cartItems.containsKey(item.getItemId()))
				.flatMap(item -> {
					final Integer requested = cartItems.get(item.getItemId());
					if (requested == 0) {
						// DELETE
						return this.cartApi.deleteCartItemByCartIdAndItemId(cart.getCartId(), item.getItemId().toString());
					}
					else if (!requested.equals(item.getQuantity())) {
						// UPDATE
						final CartItemRequest cartItemRequest = new CartItemRequest()
								.itemId(item.getItemId().toString())
								.unitPrice(item.getUnitPrice())
								.quantity(requested);
						return this.cartApi.patchItemsByCustomerId(cart.getCartId(), cartItemRequest);
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
	public Mono<String> viewCart(Cart cart, Model model) {
		final Mono<Cart> latestCart = cart.retrieveLatest(this.catalogApi);
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
