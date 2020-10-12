package lol.maki.socks.cart.web;

import java.util.List;
import java.util.Optional;

import lol.maki.socks.cart.Cart;
import lol.maki.socks.cart.CartItem;
import lol.maki.socks.cart.CartMapper;
import lol.maki.socks.cart.CartService;
import lol.maki.socks.cart.spec.CartItemRequest;
import lol.maki.socks.cart.spec.CartItemResponse;
import lol.maki.socks.cart.spec.CartResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@CrossOrigin
public class CartController {
	private final CartService cartService;

	private final CartMapper cartMapper;

	public CartController(CartService cartService, CartMapper cartMapper) {
		this.cartService = cartService;
		this.cartMapper = cartMapper;
	}

	@DeleteMapping(path = "/carts/{customerId}")
	public ResponseEntity<Void> deleteCartByCustomerId(@PathVariable("customerId") String customerId) {
		this.cartMapper.deleteCartByCustomerId(customerId);
		return ResponseEntity.status(ACCEPTED).build();
	}

	@DeleteMapping(path = "/carts/{customerId}/items/{itemId}")
	public ResponseEntity<Void> deleteCartItemByCartIdAndItemId(@PathVariable("customerId") String customerId, @PathVariable("itemId") String itemId) {
		this.cartService.deleteCartItem(customerId, itemId);
		return ResponseEntity.status(ACCEPTED).build();
	}

	@GetMapping(path = "/carts/{customerId}")
	public ResponseEntity<CartResponse> getCartByCustomerId(@PathVariable("customerId") String customerId) {
		final Cart cart = this.cartService.getOrCreateCart(customerId);
		return ResponseEntity.ok(this.toResponse(cart));
	}

	@GetMapping(path = "/carts/{customerId}/items/{itemId}")
	public ResponseEntity<CartItemResponse> getCartItemByCartIdAndItemId(@PathVariable("customerId") String customerId, @PathVariable("itemId") String itemId) {
		final Optional<CartItem> cartItem = this.cartMapper.findCartItemByCustomerIdAndItemId(customerId, itemId);
		return ResponseEntity.of(cartItem.map(this::toResponse));
	}

	@GetMapping(path = "/carts/{customerId}/items")
	public ResponseEntity<List<CartItemResponse>> getItemsByCustomerId(@PathVariable("customerId") String customerId) {
		final Cart cart = this.cartService.getOrCreateCart(customerId);
		return ResponseEntity.ok(this.toResponse(cart).getItems());
	}

	@GetMapping(path = "/carts/{customerId}/merge")
	public ResponseEntity<Void> mergeCartsByCustomerId(@PathVariable("customerId") String customerId, @RequestParam(value = "sessionId") String sessionId) {
		final Optional<Cart> cart = this.cartService.mergeCart(customerId, sessionId);
		return cart.map(__ -> ResponseEntity.status(ACCEPTED).<Void>build())
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PatchMapping(path = "/carts/{customerId}/items")
	public ResponseEntity<Void> patchItemsByCustomerId(@PathVariable("customerId") String customerId, @Validated @RequestBody CartItemRequest cartItemRequest) {
		final CartItem cartItem = fromRequest(cartItemRequest);
		this.cartService.replaceCartItem(customerId, cartItem);
		return ResponseEntity.status(ACCEPTED).build();
	}

	@PostMapping(path = "/carts/{customerId}/items")
	public ResponseEntity<CartItemResponse> postItemsByCustomerId(@PathVariable("customerId") String customerId, @Validated @RequestBody CartItemRequest cartItemRequest) {
		final CartItem cartItem = fromRequest(cartItemRequest);
		final CartItem merged = this.cartService.mergeCartItem(customerId, cartItem);
		return ResponseEntity.status(CREATED).body(this.toResponse(merged));
	}

	CartItem fromRequest(CartItemRequest request) {
		return new CartItem(request.getItemId(), request.getQuantity(), request.getUnitPrice());
	}

	CartItemResponse toResponse(CartItem cartItem) {
		return new CartItemResponse()
				.itemId(cartItem.itemId())
				.quantity(cartItem.quantity())
				.unitPrice(cartItem.unitPrice());
	}

	CartResponse toResponse(Cart cart) {
		return new CartResponse()
				.customerId(cart.customerId())
				.items(cart.items().stream()
						.filter(i -> i.quantity() > 0)
						.map(this::toResponse)
						.collect(toUnmodifiableList()));
	}
}
