package lol.maki.socks.cart.web;

import java.util.Map;
import java.util.UUID;

import lol.maki.socks.cart.Cart;
import lol.maki.socks.cart.CartClient;
import lol.maki.socks.cart.CartService;
import lol.maki.socks.cart.client.CartItemRequest;
import lol.maki.socks.cart.client.CartItemResponse;
import lol.maki.socks.catalog.CatalogClient;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.result.view.Rendering;

@Controller
public class CartController {
	private final CatalogClient catalogClient;

	private final CartClient cartClient;

	private final CartService cartService;

	public CartController(CatalogClient catalogClient, CartClient cartClient, CartService cartService) {
		this.catalogClient = catalogClient;
		this.cartClient = cartClient;
		this.cartService = cartService;
	}

	@PostMapping(path = "cart")
	public Mono<String> addCart(@ModelAttribute AddCartItemForm cartItem, Cart cart, Model model) {
		return this.catalogClient.getSock(cartItem.getId())
				.map(item -> new CartItemRequest()
						.itemId(item.getId().toString())
						.quantity(cartItem.getQuantity())
						.unitPrice(item.getPrice()))
				.flatMap(item -> this.cartClient.addCartItem(cart.getCartId(), item))
				.thenReturn("redirect:/cart");
	}

	@PostMapping(path = "cart", params = "delete")
	public Mono<String> deleteCartItem(@ModelAttribute DeleteCartItemForm cartItem, Cart cart) {
		return this.cartClient.deleteCartItem(cart.getCartId(), cartItem.getId())
				.thenReturn("redirect:/cart");
	}

	@PostMapping(path = "cart", params = "update")
	public Mono<String> updateCart(@ModelAttribute UpdateCartForm cartForm, Cart cart) {
		final Mono<Cart> latestCart = this.cartService.retrieveLatest(cart);
		final Map<UUID, Integer> cartItems = cartForm.getCartItems();
		return latestCart.flatMapIterable(Cart::getItems)
				.filter(item -> cartItems.containsKey(item.getItemId()))
				.flatMap(item -> {
					final Integer requested = cartItems.get(item.getItemId());
					if (requested == 0) {
						// DELETE
						return this.cartClient.deleteCartItem(cart.getCartId(), item.getItemId());
					}
					else if (!requested.equals(item.getQuantity())) {
						// UPDATE
						final CartItemRequest cartItemRequest = new CartItemRequest()
								.itemId(item.getItemId().toString())
								.unitPrice(item.getUnitPrice())
								.quantity(requested);
						return this.cartClient.patchCartItem(cart.getCartId(), cartItemRequest);
					}
					return Mono.empty();
				})
				.collectList()
				.thenReturn("redirect:/cart");
	}

	@PostMapping(path = "cart", params = "coupon")
	public Mono<String> applyCoupon(Model model) {
		return Mono.just("redirect:/cart");
	}

	@GetMapping(path = "cart")
	public Mono<String> viewCart(Cart cart, Model model) {
		final Mono<Cart> latestCart = this.cartService.retrieveLatest(cart);
		model.addAttribute("cart", latestCart);
		return Mono.just("shopping-cart");
	}

	@ResponseBody
	@PostMapping(path = "cart", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<CartItemResponse> addCart(@RequestBody AddCartItemForm cartItem, Cart cart) {
		return this.catalogClient.getSock(cartItem.getId())
				.map(item -> new CartItemRequest()
						.itemId(item.getId().toString())
						.quantity(cartItem.getQuantity())
						.unitPrice(item.getPrice()))
				.flatMap(item -> this.cartClient.addCartItem(cart.getCartId(), item));
	}

	@ResponseBody
	@GetMapping(path = "cart", produces = MediaType.APPLICATION_JSON_VALUE)
	public Cart viewCart(Cart cart) {
		return cart;
	}

	@ResponseBody
	@GetMapping(path = "cart", params = "latest", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<Cart> latestCart(Cart cart) {
		return this.cartService.retrieveLatest(cart);
	}

	@ExceptionHandler(RuntimeException.class)
	Rendering handleException(RuntimeException e, Cart cart) {
		return Rendering
				.view("shopping-cart")
				.status(HttpStatus.SERVICE_UNAVAILABLE)
				.modelAttribute("error", "Cart is currently unavailable. Retry later. Sorry for the inconvenience.")
				.build();
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
