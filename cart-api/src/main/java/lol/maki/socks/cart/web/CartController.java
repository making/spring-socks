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
import lol.maki.socks.cart.spec.CartsApi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@CrossOrigin
public class CartController implements CartsApi {
	private final CartService cartService;

	private final CartMapper cartMapper;

	public CartController(CartService cartService, CartMapper cartMapper) {
		this.cartService = cartService;
		this.cartMapper = cartMapper;
	}

	@Override
	public ResponseEntity<Void> deleteCartByCustomerId(String customerId) {
		this.cartMapper.deleteCartByCustomerId(customerId);
		return ResponseEntity.status(ACCEPTED).build();
	}

	@Override
	public ResponseEntity<Void> deleteCartItemByCartIdAndItemId(String customerId, String itemId) {
		this.cartService.deleteCartItem(customerId, itemId);
		return ResponseEntity.status(ACCEPTED).build();
	}

	@Override
	public ResponseEntity<CartResponse> getCartByCustomerId(String customerId) {
		final Cart cart = this.cartService.getOrCreateCart(customerId);
		return ResponseEntity.ok(this.toResponse(cart));
	}

	@Override
	public ResponseEntity<CartItemResponse> getCartItemByCartIdAndItemId(String customerId, String itemId) {
		final Optional<CartItem> cartItem = this.cartMapper.findCartItemByCustomerIdAndItemId(customerId, itemId);
		return ResponseEntity.of(cartItem.map(this::toResponse));
	}

	@Override
	public ResponseEntity<List<CartItemResponse>> getItemsByCustomerId(String customerId) {
		final Cart cart = this.cartService.getOrCreateCart(customerId);
		return ResponseEntity.ok(this.toResponse(cart).getItems());
	}

	@Override
	public ResponseEntity<Void> mergeCartsByCustomerId(String customerId, String sessionId) {
		final Optional<Cart> cart = this.cartService.mergeCart(customerId, sessionId);
		return cart.map(__ -> ResponseEntity.status(ACCEPTED).<Void>build())
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<Void> patchItemsByCustomerId(String customerId, CartItemRequest cartItemRequest) {
		final CartItem cartItem = fromRequest(cartItemRequest);
		this.cartService.replaceCartItem(customerId, cartItem);
		return ResponseEntity.status(ACCEPTED).build();
	}

	@Override
	public ResponseEntity<CartItemResponse> postItemsByCustomerId(String customerId, CartItemRequest cartItemRequest) {
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
