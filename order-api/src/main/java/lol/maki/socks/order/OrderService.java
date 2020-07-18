package lol.maki.socks.order;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import lol.maki.socks.cart.client.CartApi;
import lol.maki.socks.payment.client.AuthorizationRequest;
import lol.maki.socks.payment.client.PaymentApi;
import lol.maki.socks.shipping.client.ShipmentApi;
import lol.maki.socks.shipping.client.ShipmentRequest;
import lol.maki.socks.shipping.client.ShipmentResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.IdGenerator;

@Service
public class OrderService {
	private final CartApi cartApi;

	private final PaymentApi paymentApi;

	private final ShipmentApi shipmentApi;

	private final OrderMapper orderMapper;

	private final IdGenerator idGenerator;

	private final Clock clock;

	public OrderService(CartApi cartApi, PaymentApi paymentApi, ShipmentApi shipmentApi, OrderMapper orderMapper, IdGenerator idGenerator, Clock clock) {
		this.cartApi = cartApi;
		this.paymentApi = paymentApi;
		this.shipmentApi = shipmentApi;
		this.orderMapper = orderMapper;
		this.idGenerator = idGenerator;
		this.clock = clock;
	}

	@Transactional
	public Order placeOrder(URI customerUri, URI addressUri, URI cardUri, URI itemsUri) {
		final String orderId = Order.newOrderId(this.idGenerator::generateId);
		final String itemsPath = itemsUri.getPath();
		final String customerId = itemsPath.substring(7, itemsPath.length() - 6);
		final Mono<Customer> customerMono = this.retrieveCustomer(customerUri);
		final Mono<Address> addressMono = this.retrieveAddress(addressUri);
		final Mono<Card> cardMono = this.retrieveCard(cardUri);
		final Flux<Item> itemFlux = this.cartApi.getItemsByCustomerId(customerId)
				.map(item -> ImmutableItem.builder()
						.itemId(item.getItemId())
						.orderId(orderId)
						.quantity(item.getQuantity())
						.unitPrice(item.getUnitPrice())
						.build());
		final Order preOrder = Mono.zip(customerMono, addressMono, cardMono, itemFlux.collectList())
				.map(result -> {
					final Customer customer = result.getT1();
					final Address address = result.getT2();
					final Card card = result.getT3();
					final List<Item> items = result.getT4();
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
				})
				.flatMap(order -> {
					final AuthorizationRequest authorizationRequest = new AuthorizationRequest().amount(order.total());
					return this.paymentApi.authorizePayment(authorizationRequest)
							.flatMap(authorizationResponse -> {
								if (authorizationResponse.getAuthorization().getAuthorised()) {
									return Mono.just(order);
								}
								else {
									return Mono.error(new PaymentUnauthorizedException(authorizationResponse.getAuthorization().getMessage()));
								}
							});
				}).block();
		final ShipmentRequest shipmentRequest = new ShipmentRequest().orderId(orderId).itemCount(preOrder.itemCount());
		final ShipmentResponse shipmentResponse = this.shipmentApi.postShipping(shipmentRequest).block();
		Order order;
		try {
			order = ImmutableOrder.builder()
					.from(preOrder)
					.shipment(ImmutableShipment.builder()
							.carrier(shipmentResponse.getCarrier())
							.trackingNumber(shipmentResponse.getTrackingNumber())
							.deliveryDate(shipmentResponse.getDeliveryDate())
							.build())
					.build();
			this.orderMapper.insert(order);
		}
		catch (RuntimeException e) {
			// TODO cancel shipment request
			throw e;
		}
		try {
			this.cartApi.deleteCartByCustomerId(customerId)
					.block();
		}
		catch (RuntimeException ignored) {

		}
		return order;
	}

	Mono<Customer> retrieveCustomer(URI customerUri) {
		return Mono.just(ImmutableCustomer.builder()
				.id("1234")
				.firstName("John")
				.lastName("Doe")
				.username("jdoe")
				.build());
	}

	Mono<Address> retrieveAddress(URI addressUri) {
		return Mono.just(ImmutableAddress.builder()
				.number("123")
				.street("Street")
				.city("City")
				.country("Country")
				.postcode("1111111")
				.build());
	}

	Mono<Card> retrieveCard(URI cardUri) {
		return Mono.just(ImmutableCard.builder()
				.longNum("4111111111111111")
				.ccv("123")
				.expires(LocalDate.of(2024, 1, 1))
				.build());
	}
}
