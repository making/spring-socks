package lol.maki.socks.order;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lol.maki.socks.cart.client.CartApi;
import lol.maki.socks.cart.client.CartItemResponse;
import lol.maki.socks.customer.CustomerClient;
import lol.maki.socks.payment.client.AuthorizationRequest;
import lol.maki.socks.payment.client.PaymentApi;
import lol.maki.socks.shipping.client.ShipmentApi;
import lol.maki.socks.shipping.client.ShipmentRequest;
import lol.maki.socks.shipping.client.ShipmentResponse;
import lol.maki.socks.user.client.CustomerAddressResponse;
import lol.maki.socks.user.client.CustomerCardResponse;
import lol.maki.socks.user.client.CustomerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.IdGenerator;

@Service
public class OrderService {
	private final CartApi cartApi;

	private final PaymentApi paymentApi;

	private final ShipmentApi shipmentApi;

	private final CustomerClient customerClient;

	private final OrderMapper orderMapper;

	private final IdGenerator idGenerator;

	private final Clock clock;

	private final CircuitBreaker cartCircuitBreaker;

	private final CircuitBreaker paymentCircuitBreaker;

	private final CircuitBreaker shipmentCircuitBreaker;

	public OrderService(CartApi cartApi, PaymentApi paymentApi, ShipmentApi shipmentApi, CustomerClient customerClient, OrderMapper orderMapper, IdGenerator idGenerator, Clock clock, CircuitBreakerRegistry circuitBreakerRegistry) {
		this.cartApi = cartApi;
		this.paymentApi = paymentApi;
		this.shipmentApi = shipmentApi;
		this.customerClient = customerClient;
		this.orderMapper = orderMapper;
		this.idGenerator = idGenerator;
		this.clock = clock;
		this.cartCircuitBreaker = circuitBreakerRegistry.circuitBreaker("cart");
		this.paymentCircuitBreaker = circuitBreakerRegistry.circuitBreaker("payment");
		this.shipmentCircuitBreaker = circuitBreakerRegistry.circuitBreaker("shipment");
	}

	@Transactional
	public Order placeOrder(String customerId, UUID addressId, UUID cardId) {
		final String orderId = Order.newOrderId(this.idGenerator::generateId);
		final Mono<Tuple3<Customer, Address, Card>> customerMono = this.customerClient.retrieveCustomer(customerId)
				.switchIfEmpty(Mono.error(() -> new IllegalOrderException("The requested customer is not found (customerId=" + customerId + ")")))
				.flatMap(r -> {
					final Customer c = this.toCustomer(customerId, r);
					final Mono<Address> address = r.getAddresses() != null ?
							Mono.justOrEmpty(r.getAddresses()
									.stream()
									.filter(a -> Objects.equals(a.getAddressId(), addressId))
									.findAny())
									.map(this::toAddress) :
							Mono.empty();
					final Mono<Card> card = r.getCards() != null ?
							Mono.justOrEmpty(r.getCards()
									.stream()
									.filter(d -> Objects.equals(d.getCardId(), cardId))
									.findAny())
									.map(this::toCard) :
							Mono.empty();
					return Mono.zip(
							address.switchIfEmpty(Mono.error(() -> new IllegalOrderException("The requested address is not found (address=" + addressId + ")"))),
							card.switchIfEmpty(Mono.error(() -> new IllegalOrderException("The requested card is not found (card=" + cardId + ")"))))
							.map(t -> Tuples.of(c, t.getT1(), t.getT2()));
				});
		final Flux<Item> itemFlux = this.cartApi.getItemsByCustomerId(customerId)
				.transformDeferred(CircuitBreakerOperator.of(this.cartCircuitBreaker))
				.map(item -> this.toItem(orderId, item))
				.switchIfEmpty(Mono.error(() -> new IllegalOrderException("The requested cart is not found (customerId=" + customerId + ")")));
		final Mono<Order> preOrderMono = Mono.zip(customerMono, itemFlux.collectList())
				.map(result -> {
					final Customer customer = result.getT1().getT1();
					final Address address = result.getT1().getT2();
					final Card card = result.getT1().getT3();
					final List<Item> items = result.getT2();
					return this.createPreOrder(orderId, customer, address, card, items);
				})
				.flatMap(order -> {
					final AuthorizationRequest authorizationRequest = new AuthorizationRequest().amount(order.total());
					return this.paymentApi.authorizePayment(authorizationRequest)
							.transformDeferred(CircuitBreakerOperator.of(this.paymentCircuitBreaker))
							.flatMap(authorizationResponse -> {
								if (authorizationResponse.getAuthorization().getAuthorised()) {
									return Mono.just(order);
								}
								else {
									return Mono.error(new PaymentUnauthorizedException(authorizationResponse.getAuthorization().getMessage()));
								}
							});
				});
		final Mono<Order> orderMono = preOrderMono.flatMap(preOrder -> {
			final ShipmentRequest shipmentRequest = new ShipmentRequest().orderId(orderId).itemCount(preOrder.itemCount());
			return this.shipmentApi.postShipping(shipmentRequest)
					.transformDeferred(CircuitBreakerOperator.of(this.shipmentCircuitBreaker))
					.map(shipmentResponse -> this.createOrder(preOrder, shipmentResponse));
		});
		final Order order;
		try {
			order = orderMono.block();
			this.orderMapper.insert(order);
		}
		catch (RuntimeException e) {
			// TODO cancel shipment request
			throw e;
		}
		try {
			this.cartApi.deleteCartByCustomerId(customerId)
					.transformDeferred(CircuitBreakerOperator.of(this.cartCircuitBreaker))
					.block();
		}
		catch (RuntimeException ignored) {

		}
		return order;
	}

	Customer toCustomer(String customerId, CustomerResponse r) {
		return ImmutableCustomer.builder()
				.id(customerId)
				.firstName(r.getFirstName())
				.lastName(r.getLastName())
				.username(r.getUsername())
				.build();
	}

	Address toAddress(CustomerAddressResponse r) {
		return ImmutableAddress.builder()
				.number(r.getNumber())
				.street(r.getStreet())
				.city(r.getCity())
				.country(r.getCountry())
				.postcode(r.getPostcode())
				.build();
	}

	Card toCard(CustomerCardResponse r) {
		return ImmutableCard.builder()
				.longNum(r.getLongNum())
				.ccv(r.getCcv())
				.expires(r.getExpires())
				.build();
	}

	Item toItem(String orderId, CartItemResponse item) {
		return ImmutableItem.builder()
				.itemId(item.getItemId())
				.orderId(orderId)
				.quantity(item.getQuantity())
				.unitPrice(item.getUnitPrice())
				.build();
	}

	Order createPreOrder(String orderId, Customer customer, Address address, Card card, List<Item> items) {
		return ImmutableOrder.builder()
				.id(orderId)
				.customer(customer)
				.address(address)
				.card(card)
				.items(items)
				.date(OffsetDateTime.now(this.clock))
				.status(OrderStatus.CREATED)
				.shipment(ImmutableShipment.builder().carrier("dummy").deliveryDate(LocalDate.MIN).trackingNumber(UUID.randomUUID()).build())
				.build();
	}

	Order createOrder(Order preOrder, ShipmentResponse shipmentResponse) {
		return ImmutableOrder.builder()
				.from(preOrder)
				.shipment(ImmutableShipment.builder()
						.carrier(shipmentResponse.getCarrier())
						.trackingNumber(shipmentResponse.getTrackingNumber())
						.deliveryDate(shipmentResponse.getDeliveryDate())
						.build())
				.build();
	}
}
