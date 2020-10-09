package lol.maki.socks.shop;

import lol.maki.socks.cart.Cart;
import lol.maki.socks.catalog.client.CatalogApi;
import lol.maki.socks.config.SockProps;
import lol.maki.socks.order.Order;
import lol.maki.socks.order.OrderService;
import lol.maki.socks.order.client.OrderApi;
import reactor.core.publisher.Mono;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CheckoutController {
	private final SockProps props;

	private final OrderApi orderApi;

	private final CatalogApi catalogApi;

	private final OrderService orderService;

	public CheckoutController(SockProps props, OrderApi orderApi, CatalogApi catalogApi, OrderService orderService) {
		this.props = props;
		this.orderApi = orderApi;
		this.catalogApi = catalogApi;
		this.orderService = orderService;
	}

	@ModelAttribute
	public Order setupForm() {
		return new Order();
	}

	@GetMapping(path = "checkout")
	public Mono<String> checkoutForm(Cart cart, Model model) {
		final Mono<Cart> latest = cart.retrieveLatest(this.catalogApi);
		model.addAttribute("cart", latest);
		return Mono.just("checkout");
	}

	@PostMapping(path = "checkout")
	public Mono<String> checkout(Cart cart, Model model, Order order, @RegisteredOAuth2AuthorizedClient("sock") OAuth2AuthorizedClient authorizedClient) {
		// TODO validation
		// return this.checkoutForm(cart, model);
		return this.orderService.placeOrder(cart, order, authorizedClient)
				.thenReturn("redirect:/");
	}
}
