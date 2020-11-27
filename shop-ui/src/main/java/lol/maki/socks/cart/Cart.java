package lol.maki.socks.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import lol.maki.socks.catalog.client.SockResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.web.reactive.function.client.WebClient;

import static java.math.BigDecimal.ZERO;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

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

	public static Cart empty(String cartId) {
		return new Cart(cartId, new ArrayList<>());
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

	public Mono<Cart> retrieveLatest(String catalogUrl, WebClient webClient, OAuth2AuthorizedClient authorizedClient) {
		return Flux.fromIterable(this.getItems())
				.flatMap(i -> webClient.get()
						.uri(catalogUrl, b -> b.path("catalogue/{id}").build(i.getItemId()))
						.attributes(oauth2AuthorizedClient(authorizedClient))
						.retrieve()
						.bodyToMono(SockResponse.class)
						.map(s -> i.setNameAndImageUrl(s.getName(), s.getImageUrl().get(0))))
				.collectList()
				.map(items -> new Cart(this.getCartId(), items))
				.onErrorReturn(Cart.empty(cartId));
	}

	@Override
	public String toString() {
		return "Cart{" +
				"cartItems=" + cartItems +
				'}';
	}
}
