package lol.maki.socks.cart.web;

import java.util.List;
import java.util.Optional;

import lol.maki.socks.cart.Cart;
import lol.maki.socks.cart.CartItem;
import lol.maki.socks.cart.CartMapper;
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
	private final CartMapper cartMapper;

	public CartController(CartMapper cartMapper) {
		this.cartMapper = cartMapper;
	}

	@Override
	public ResponseEntity<Void> deleteCartByCustomerId(String customerId) {
		this.cartMapper.deleteCartByCustomerId(customerId);
		return ResponseEntity.status(ACCEPTED).build();
	}

	@Override
	public ResponseEntity<Void> deleteCartItemByCartIdAndItemId(String customerId, String itemId) {
		final Optional<Cart> cart = this.cartMapper.findCartByCustomerId(customerId);
		return cart.map(c -> c.removeItem(itemId))
				.map(__ -> ResponseEntity.status(ACCEPTED).<Void>build())
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<CartResponse> getCartByCustomerId(String customerId) {
		final Optional<Cart> cart = this.cartMapper.findCartByCustomerId(customerId);
		return ResponseEntity.of(cart.map(this::toResponse));
	}

	@Override
	public ResponseEntity<CartItemResponse> getCartItemByCartIdAndItemId(String customerId, String itemId) {
		final Optional<CartItem> cartItem = this.cartMapper.findCartItemByCustomerIdAndItemId(customerId, itemId);
		return ResponseEntity.of(cartItem.map(this::toResponse));
	}

	@Override
	public ResponseEntity<List<CartItemResponse>> getItemsByCustomerId(String customerId) {
		final Optional<Cart> cart = this.cartMapper.findCartByCustomerId(customerId);
		return ResponseEntity.of(cart.map(this::toResponse)
				.map(CartResponse::getItems));
	}

	@Override
	public ResponseEntity<Void> mergeCartsByCustomerId(String customerId, String sessionId) {
		final Optional<Cart> cart = this.cartMapper.findCartByCustomerId(customerId);
		final Optional<Cart> sessionCart = this.cartMapper.findCartByCustomerId(sessionId);
		final Optional<Cart> mergedCart = cart.flatMap(c -> sessionCart.map(c::mergeCart));
		return mergedCart.map(__ -> ResponseEntity.status(ACCEPTED).<Void>build())
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<Void> patchItemsByCustomerId(String customerId, CartItemRequest cartItemRequest) {
		final CartItem cartItem = fromRequest(cartItemRequest);
		final Optional<Cart> cart = this.cartMapper.findCartByCustomerId(customerId);
		cart.map(c -> {
			c.replaceItem(cartItem);
			return c;
		}).ifPresent(this.cartMapper::updateCart);
		return ResponseEntity.status(ACCEPTED).build();
	}

	@Override
	public ResponseEntity<CartItemResponse> postItemsByCustomerId(String customerId, CartItemRequest cartItemRequest) {
		final Optional<Cart> cart = this.cartMapper.findCartByCustomerId(customerId);
		final CartItem cartItem = fromRequest(cartItemRequest);
		final Optional<CartItem> updatedItem = cart.map(c -> {
			final CartItem updated = c.mergeItem(cartItem);
			this.cartMapper.updateCart(c);
			return updated;
		});
		return updatedItem.map(this::toResponse)
				.map(b -> ResponseEntity.status(CREATED).body(b))
				.orElseGet(() -> ResponseEntity.notFound().build());
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
				.items(cart.items().stream().map(this::toResponse).collect(toUnmodifiableList()));
	}
}
