package lol.maki.socks.cart;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import lol.maki.socks.catalog.client.CatalogApi;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.math.BigDecimal.ZERO;

public class Cart {
	private final String cartId;

	private final List<CartItem> cartItems;

	public Cart(String cartId, List<CartItem> cartItems) {
		this.cartId = cartId;
		this.cartItems = cartItems;
	}

	public boolean hasSessionId() {
		return this.cartId.startsWith("_");
	}

	public static String generateSessionId(Supplier<UUID> uuidgen) {
		return "_" + uuidgen.get();
	}

	public String getCartId() {
		return this.cartId;
	}

	public int getItemSize() {
		return this.cartItems.stream().mapToInt(CartItem::getQuantity).sum();
	}

	public List<CartItem> getItems() {
		return this.cartItems;
	}

	public BigDecimal getTotal() {
		return this.cartItems
				.stream()
				.map(CartItem::getTotal)
				.reduce(ZERO, BigDecimal::add);
	}

	public Mono<Cart> retrieveLatest(CatalogApi catalogApi) {
		return Flux.fromIterable(this.getItems())
				.flatMap(i -> catalogApi.getSock(i.getItemId())
						.map(s -> i.setNameAndImageUrl(s.getName(), s.getImageUrl().get(0))))
				.collectList()
				.map(items -> new Cart(this.getCartId(), items));
	}

	@Override
	public String toString() {
		return "Cart{" +
				"cartItems=" + cartItems +
				'}';
	}
}
