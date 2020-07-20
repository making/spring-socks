package lol.maki.socks.order;

import java.net.URI;
import java.util.List;

import lol.maki.socks.LoggedInUser;
import lol.maki.socks.config.SockProps;
import lol.maki.socks.order.client.HypermediaLink;
import lol.maki.socks.order.client.OrderApi;
import lol.maki.socks.order.client.OrderRequest;
import lol.maki.socks.order.client.OrderResponse;
import lol.maki.socks.order.client.OrderResponseLinks;
import reactor.core.publisher.Mono;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
public class OrderController {
	private final SockProps props;

	private final OrderApi orderApi;

	public OrderController(SockProps props, OrderApi orderApi) {
		this.props = props;
		this.orderApi = orderApi;
	}

	@GetMapping(path = "orders")
	public Mono<List<OrderResponse>> gerOrders(UriComponentsBuilder builder) {
		final String customerId = LoggedInUser.customerId();
		return this.orderApi.searchOrdersByCustomerId(customerId)
				.map(r -> this.replaceSelfLink(r, builder))
				.collectList();
	}

	@GetMapping(path = "orders/{orderId}")
	public Mono<OrderResponse> getOrder(@PathVariable("orderId") String orderId, UriComponentsBuilder builder) {
		return this.orderApi.getOrder(orderId)
				.map(r -> this.replaceSelfLink(r, builder));
	}

	@PostMapping(path = "orders")
	public Mono<ResponseEntity<OrderResponse>> placeOrder(UriComponentsBuilder builder) {
		return this.orderApi.createOrder(new OrderRequest()
				.customer(URI.create("http://example.com"))
				.address(URI.create("http://example.com"))
				.card(URI.create("http://example.com"))
				.items(UriComponentsBuilder.fromHttpUrl(props.getCartUrl())
						.pathSegment("carts/{customerId}/items")
						.build(LoggedInUser.customerId())))
				.map(r -> this.replaceSelfLink(r, builder))
				.map(order -> ResponseEntity.status(CREATED).body(order));
	}

	OrderResponse replaceSelfLink(OrderResponse response, UriComponentsBuilder builder) {
		final URI replaced = builder.replacePath(response.getLinks().getSelf().getHref().getPath()).build("");
		return response.links(new OrderResponseLinks().self(new HypermediaLink().rel("self").href(replaced)));
	}
}
