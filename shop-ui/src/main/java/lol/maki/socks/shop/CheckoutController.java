package lol.maki.socks.shop;

import lol.maki.socks.cart.Cart;
import lol.maki.socks.config.SockProps;
import lol.maki.socks.order.Order;
import lol.maki.socks.order.OrderService;
import reactor.core.publisher.Mono;

import org.springframework.http.ResponseCookie;
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
import org.springframework.web.server.ServerWebExchange;

import static lol.maki.socks.cart.CartHandlerMethodArgumentResolver.CART_ID_COOKIE_NAME;

@Controller
public class CheckoutController {
	private final OrderService orderService;

	private final WebClient webClient;

	private final SockProps props;

	public CheckoutController(OrderService orderService, Builder builder, ReactiveOAuth2AuthorizedClientManager authorizedClientManager, SockProps props) {
		this.orderService = orderService;
		this.webClient = builder
				.filter(new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager))
				.build();
		this.props = props;
	}

	@ModelAttribute
	public Order setupForm() {
		return new Order();
	}

	@GetMapping(path = "checkout")
	public Mono<String> checkoutForm(Cart cart, Model model, @RegisteredOAuth2AuthorizedClient("sock") OAuth2AuthorizedClient authorizedClient) {
		final Mono<Cart> latestCart = cart.retrieveLatest(props.getCatalogUrl(), this.webClient, authorizedClient);
		model.addAttribute("cart", latestCart);
		return Mono.just("checkout");
	}

	@PostMapping(path = "checkout")
	public Mono<String> checkout(Cart cart, Model model, Order order, @RegisteredOAuth2AuthorizedClient("sock") OAuth2AuthorizedClient authorizedClient, ServerWebExchange exchange) {
		// TODO validation
		// return this.checkoutForm(cart, model);
		return this.orderService.placeOrder(cart, order, authorizedClient)
				.doOnSuccess(__ -> /* Delete Cookie */
						exchange.getResponse().addCookie(ResponseCookie.from(CART_ID_COOKIE_NAME, "deleted")
								.maxAge(0)
								.httpOnly(true)
								.path("/")
								.build()))
				.thenReturn("redirect:/");
	}
}
