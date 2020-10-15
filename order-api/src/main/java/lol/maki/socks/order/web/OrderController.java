package lol.maki.socks.order.web;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import lol.maki.socks.order.Address;
import lol.maki.socks.order.Card;
import lol.maki.socks.order.Customer;
import lol.maki.socks.order.IllegalOrderException;
import lol.maki.socks.order.Order;
import lol.maki.socks.order.OrderMapper;
import lol.maki.socks.order.OrderService;
import lol.maki.socks.order.PaymentUnauthorizedException;
import lol.maki.socks.order.Shipment;
import lol.maki.socks.order.spec.OrderRequest;
import lol.maki.socks.order.spec.OrderResponse;
import lol.maki.socks.order.spec.OrderResponse.StatusEnum;
import lol.maki.socks.order.spec.OrderResponseAddress;
import lol.maki.socks.order.spec.OrderResponseCard;
import lol.maki.socks.order.spec.OrderResponseCustomer;
import lol.maki.socks.order.spec.OrderResponseItem;
import lol.maki.socks.order.spec.OrderResponseShipment;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@CrossOrigin
public class OrderController {
	private final OrderMapper orderMapper;

	private final OrderService orderService;

	public OrderController(OrderMapper orderMapper, OrderService orderService) {
		this.orderMapper = orderMapper;
		this.orderService = orderService;
	}

	@PostMapping(path = "/orders")
	public ResponseEntity<OrderResponse> createOrder(@AuthenticationPrincipal Jwt jwt, @Validated @RequestBody OrderRequest req, UriComponentsBuilder builder) {
		final String customerId = jwt.getSubject();
		final UUID addressId = req.getAddressId();
		final UUID cardId = req.getCardId();
		final Order order = this.orderService.placeOrder(customerId, addressId, cardId);
		final URI location = builder.replacePath("orders/{orderId}").build(order.id());
		return ResponseEntity.created(location).body(toResponse(order));
	}

	@GetMapping(path = "/orders/{id}")
	public ResponseEntity<?> getOrder(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") String id) {
		final String customerId = jwt.getSubject();
		return ResponseEntity.of(this.orderMapper.findOne(id)
				.filter(order -> Objects.equals(order.customer().id(), customerId))
				.map(this::toResponse));
	}

	@GetMapping(path = "/orders")
	public ResponseEntity<List<OrderResponse>> getOrdersByCustomerId(@RequestParam(value = "customerId") String customerId) {
		return ResponseEntity.ok(this.orderMapper.findByCustomerId(customerId)
				.stream()
				.map(this::toResponse)
				.collect(toUnmodifiableList()));
	}

	@ExceptionHandler(PaymentUnauthorizedException.class)
	public ResponseEntity<Map<String, String>> handlePaymentUnauthorizedException(PaymentUnauthorizedException e) {
		return ResponseEntity.status(UNAUTHORIZED).body(Map.of("message", e.getMessage()));
	}

	@ExceptionHandler(IllegalOrderException.class)
	public ResponseEntity<Map<String, String>> handleIllegalOrderException(IllegalOrderException e) {
		return ResponseEntity.status(BAD_REQUEST).body(Map.of("message", e.getMessage()));
	}

	OrderResponse toResponse(Order order) {
		final Customer customer = order.customer();
		final Address address = order.address();
		final Card card = order.card();
		final Shipment shipment = order.shipment();
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
				.status(StatusEnum.valueOf(order.status().name()));
	}
}
