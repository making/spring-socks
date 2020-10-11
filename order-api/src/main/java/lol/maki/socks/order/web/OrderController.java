package lol.maki.socks.order.web;

import java.net.URI;
import java.util.List;
import java.util.Map;

import lol.maki.socks.order.Address;
import lol.maki.socks.order.Card;
import lol.maki.socks.order.Customer;
import lol.maki.socks.order.Order;
import lol.maki.socks.order.OrderMapper;
import lol.maki.socks.order.OrderService;
import lol.maki.socks.order.PaymentUnauthorizedException;
import lol.maki.socks.order.Shipment;
import lol.maki.socks.order.spec.HypermediaLink;
import lol.maki.socks.order.spec.OrderRequest;
import lol.maki.socks.order.spec.OrderResponse;
import lol.maki.socks.order.spec.OrderResponse.StatusEnum;
import lol.maki.socks.order.spec.OrderResponseAddress;
import lol.maki.socks.order.spec.OrderResponseCard;
import lol.maki.socks.order.spec.OrderResponseCustomer;
import lol.maki.socks.order.spec.OrderResponseItem;
import lol.maki.socks.order.spec.OrderResponseLinks;
import lol.maki.socks.order.spec.OrderResponseShipment;
import lol.maki.socks.order.spec.OrdersApi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@CrossOrigin
public class OrderController implements OrdersApi {
	private final OrderMapper orderMapper;

	private final OrderService orderService;

	public OrderController(OrderMapper orderMapper, OrderService orderService) {
		this.orderMapper = orderMapper;
		this.orderService = orderService;
	}

	@Override
	public ResponseEntity<OrderResponse> createOrder(OrderRequest req) {
		
		final Order order = this.orderService.placeOrder(req.getCustomer(), req.getAddress(), req.getCard(), req.getItems());
		final ServletUriComponentsBuilder uriComponentsBuilder = ServletUriComponentsBuilder.fromCurrentRequest();
		final URI location = uriComponentsBuilder.replacePath("orders/{orderId}").build(order.id());
		return ResponseEntity.created(location).body(toResponse(order));
	}

	@Override
	public ResponseEntity<OrderResponse> getOrder(String id) {
		return ResponseEntity.of(this.orderMapper.findOne(id).map(this::toResponse));
	}

	@Override
	public ResponseEntity<List<OrderResponse>> searchOrdersByCustomerId(String custId) {
		return ResponseEntity.ok(this.orderMapper.findByCustomerId(custId).stream().map(this::toResponse).collect(toUnmodifiableList()));
	}

	@ExceptionHandler(PaymentUnauthorizedException.class)
	public ResponseEntity<Map<String, String>> handlePaymentUnauthorizedException(PaymentUnauthorizedException e) {
		return ResponseEntity.status(UNAUTHORIZED).body(Map.of("message", e.getMessage()));
	}

	OrderResponse toResponse(Order order) {
		final Customer customer = order.customer();
		final Address address = order.address();
		final Card card = order.card();
		final Shipment shipment = order.shipment();
		final URI selfHref = ServletUriComponentsBuilder.fromCurrentRequest()
				.replacePath("orders/{id}")
				.query(null)
				.build(order.id());
		return new OrderResponse()
				.id(order.id())
				.customer(new OrderResponseCustomer()
						.id(customer.id())
						.firstName(customer.firstName())
						.lastName(customer.lastName())
						.username(customer.username()))
				.address(new OrderResponseAddress()
						.number(address.number())
						.street(address.street())
						.city(address.city())
						.postcode(address.postcode())
						.country(address.country()))
				.card(new OrderResponseCard()
						.longNum(card.longNum())
						.expires(card.expires())
						.ccv(card.ccv()))
				.items(order.items()
						.stream()
						.map(item -> new OrderResponseItem()
								.itemId(item.itemId())
								.quantity(item.quantity())
								.unitPrice(item.unitPrice()))
						.collect(toUnmodifiableList()))
				.shipment(new OrderResponseShipment()
						.carrier(shipment.carrier())
						.trackingNumber(shipment.trackingNumber())
						.deliveryDate(shipment.deliveryDate()))
				.date(order.date())
				.total(order.total())
				.status(StatusEnum.valueOf(order.status().name()))
				.links(new OrderResponseLinks().self(new HypermediaLink()
						.rel("self")
						.href(selfHref)));
	}
}
