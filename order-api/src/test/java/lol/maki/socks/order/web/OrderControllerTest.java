package lol.maki.socks.order.web;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lol.maki.socks.cart.client.CartApi;
import lol.maki.socks.cart.client.CartItemResponse;
import lol.maki.socks.customer.CustomerClient;
import lol.maki.socks.order.Address;
import lol.maki.socks.order.Card;
import lol.maki.socks.order.Customer;
import lol.maki.socks.order.Item;
import lol.maki.socks.order.Order;
import lol.maki.socks.order.OrderMapper;
import lol.maki.socks.order.OrderService;
import lol.maki.socks.order.OrderStatus;
import lol.maki.socks.order.Shipment;
import lol.maki.socks.payment.client.Authorization;
import lol.maki.socks.payment.client.AuthorizationResponse;
import lol.maki.socks.payment.client.PaymentApi;
import lol.maki.socks.shipping.client.ShipmentApi;
import lol.maki.socks.shipping.client.ShipmentResponse;
import lol.maki.socks.user.client.CustomerAddressResponse;
import lol.maki.socks.user.client.CustomerCardResponse;
import lol.maki.socks.user.client.CustomerResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.IdGenerator;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(properties = "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://uaa.run.pcfone.io/oauth/token", controllers = OrderController.class)
@Import({ OrderService.class, Config.class })
class OrderControllerTest {
	@Autowired
	MockMvc mockMvc;

	@MockBean
	CartApi cartApi;

	@MockBean
	ShipmentApi shipmentApi;

	@MockBean
	PaymentApi paymentApi;

	@MockBean
	CustomerClient customerClient;

	@MockBean
	OrderMapper orderMapper;

	@MockBean
	Clock clock;

	@MockBean
	IdGenerator idGenerator;

	String orderId1 = "11111111";

	String orderId2 = "22222222";

	Customer customer1 = new Customer(
			"1234",
			"John",
			"Doe",
			"jdoe");

	Address address1 = new Address(
			"123",
			"Street",
			"City",
			"1111111",
			"Country");

	UUID addressId1 = UUID.fromString("3b541549-1384-4c70-b507-c65431c61650");

	Card card1 = new Card(
			"4111111111111111",
			LocalDate.of(2024, 1, 1),
			"123");

	UUID cardId1 = UUID.fromString("e8ae2ef6-3ef3-44b8-9bb2-5390c1788040");

	Item item1 = new Item(
			this.orderId1,
			"6d62d909-f957-430e-8689-b5129c0bb75e",
			2,
			new BigDecimal("17.15"));

	Item item2 = new Item(
			this.orderId1,
			"f611b671-40a3-4020-ab7f-68d56a813dc8",
			1,
			new BigDecimal("20"));

	Item item3 = new Item(
			this.orderId2,
			"0e22b596-812d-4d06-a65e-39e144299fb8",
			1,
			new BigDecimal("10.25"));

	Shipment shipment1 = new Shipment(
			"UPS",
			UUID.fromString("06df0447-8679-47fd-a184-65101acc52dc"),
			LocalDate.of(2020, 7, 17));

	Order order1 = new Order(
			this.orderId1,
			this.customer1,
			this.address1,
			this.card1,
			List.of(this.item1, this.item2),
			this.shipment1,
			LocalDate.of(2020, 7, 14).atStartOfDay().atOffset(OffsetDateTime.now().getOffset()),
			OrderStatus.SHIPPED);

	Order order2 = new Order(
			this.orderId2,
			this.customer1,
			this.address1,
			this.card1,
			List.of(this.item3),
			this.shipment1,
			LocalDate.of(2020, 7, 15).atStartOfDay().atOffset(OffsetDateTime.now().getOffset()),
			OrderStatus.PAID);

	@Test
	void createOrder() throws Exception {
		final OffsetDateTime now = OffsetDateTime.parse("2020-07-14T00:00:00+09:00");
		given(this.idGenerator.generateId()).willReturn(UUID.fromString(this.orderId1 + "-3a8a-42af-b86d-6d3f87241093"));
		given(this.clock.instant()).willReturn(now.toInstant());
		given(this.clock.getZone()).willReturn(now.getOffset());
		given(this.cartApi.getItemsByCustomerId(this.customer1.id()))
				.willReturn(Flux.just(
						new CartItemResponse()
								.itemId(this.item1.itemId())
								.quantity(this.item1.quantity())
								.unitPrice(this.item1.unitPrice()),
						new CartItemResponse()
								.itemId(this.item2.itemId())
								.quantity(this.item2.quantity())
								.unitPrice(this.item2.unitPrice())
				));
		given(this.paymentApi.authorizePayment(any()))
				.willReturn(Mono.just(new AuthorizationResponse()
						.authorization(new Authorization()
								.authorised(true)
								.message("OK"))));
		given(this.shipmentApi.postShipping(any()))
				.willReturn(Mono.just(new ShipmentResponse()
						.carrier(this.shipment1.carrier())
						.deliveryDate(this.shipment1.deliveryDate())
						.trackingNumber(this.shipment1.trackingNumber())));
		given(this.customerClient.retrieveCustomer(any()))
				.willReturn(Mono.just(new CustomerResponse()
						.username(this.customer1.username())
						.firstName(this.customer1.firstName())
						.lastName(this.customer1.lastName())
						.addAddressesItem(new CustomerAddressResponse()
								.addressId(this.addressId1)
								.number(this.address1.number())
								.street(this.address1.street())
								.city(this.address1.city())
								.postcode(this.address1.postcode())
								.country(this.address1.country()))
						.addCardsItem(new CustomerCardResponse()
								.cardId(this.cardId1)
								.longNum(this.card1.longNum())
								.expires(this.card1.expires())
								.ccv(this.card1.ccv()))));
		this.mockMvc.perform(post("/orders")
				.with(jwt()
						.jwt(builder -> builder.subject(this.customer1.id()))
						.authorities(new SimpleGrantedAuthority("SCOPE_order:write")))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(UTF_8.toString())
				.content(String.format("{\"addressId\":\"%s\",\"cardId\":\"%s\"}", this.addressId1, this.cardId1)))
				.andExpect(status().isCreated())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
				.andExpect(jsonPath("$.id").value(this.orderId1))
				.andExpect(jsonPath("$.customer.id").value(this.customer1.id()))
				.andExpect(jsonPath("$.customer.firstName").value(this.customer1.firstName()))
				.andExpect(jsonPath("$.customer.lastName").value(this.customer1.lastName()))
				.andExpect(jsonPath("$.customer.username").value(this.customer1.username()))
				.andExpect(jsonPath("$.address.number").value(this.address1.number()))
				.andExpect(jsonPath("$.address.street").value(this.address1.street()))
				.andExpect(jsonPath("$.address.city").value(this.address1.city()))
				.andExpect(jsonPath("$.address.postcode").value(this.address1.postcode()))
				.andExpect(jsonPath("$.address.country").value(this.address1.country()))
				.andExpect(jsonPath("$.card.longNum").value(this.card1.longNum()))
				.andExpect(jsonPath("$.card.expires").value(this.card1.expires().toString()))
				.andExpect(jsonPath("$.card.ccv").value(this.card1.ccv()))
				.andExpect(jsonPath("$.items.length()").value(2))
				.andExpect(jsonPath("$.items[0].itemId").value(this.item1.itemId()))
				.andExpect(jsonPath("$.items[0].quantity").value(this.item1.quantity()))
				.andExpect(jsonPath("$.items[0].unitPrice").value(is(this.item1.unitPrice()), BigDecimal.class))
				.andExpect(jsonPath("$.items[1].itemId").value(this.item2.itemId()))
				.andExpect(jsonPath("$.items[1].quantity").value(this.item2.quantity()))
				.andExpect(jsonPath("$.items[1].unitPrice").value(is(this.item2.unitPrice()), BigDecimal.class))
				.andExpect(jsonPath("$.shipment.carrier").value(this.shipment1.carrier()))
				.andExpect(jsonPath("$.shipment.trackingNumber").value(this.shipment1.trackingNumber().toString()))
				.andExpect(jsonPath("$.shipment.deliveryDate").value(this.shipment1.deliveryDate().toString()))
				.andExpect(jsonPath("$.date").value("2020-07-14T00:00:00+09:00"))
				.andExpect(jsonPath("$.status").value("CREATED"));
	}

	@Test
	void getOrder() throws Exception {
		given(this.orderMapper.findOne(this.orderId1)).willReturn(Optional.of(this.order1));
		this.mockMvc.perform(get("/orders/{orderId}", this.orderId1)
				.with(jwt()
						.jwt(builder -> builder.subject(this.customer1.id()))
						.authorities(new SimpleGrantedAuthority("SCOPE_order:read"))))
				.andExpect(status().isOk())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
				.andExpect(jsonPath("$.id").value(this.orderId1))
				.andExpect(jsonPath("$.customer.id").value(this.customer1.id()))
				.andExpect(jsonPath("$.customer.firstName").value(this.customer1.firstName()))
				.andExpect(jsonPath("$.customer.lastName").value(this.customer1.lastName()))
				.andExpect(jsonPath("$.customer.username").value(this.customer1.username()))
				.andExpect(jsonPath("$.address.number").value(this.address1.number()))
				.andExpect(jsonPath("$.address.street").value(this.address1.street()))
				.andExpect(jsonPath("$.address.city").value(this.address1.city()))
				.andExpect(jsonPath("$.address.postcode").value(this.address1.postcode()))
				.andExpect(jsonPath("$.address.country").value(this.address1.country()))
				.andExpect(jsonPath("$.card.longNum").value(this.card1.longNum()))
				.andExpect(jsonPath("$.card.expires").value(this.card1.expires().toString()))
				.andExpect(jsonPath("$.card.ccv").value(this.card1.ccv()))
				.andExpect(jsonPath("$.items.length()").value(2))
				.andExpect(jsonPath("$.items[0].itemId").value(this.item1.itemId()))
				.andExpect(jsonPath("$.items[0].quantity").value(this.item1.quantity()))
				.andExpect(jsonPath("$.items[0].unitPrice").value(is(this.item1.unitPrice()), BigDecimal.class))
				.andExpect(jsonPath("$.items[1].itemId").value(this.item2.itemId()))
				.andExpect(jsonPath("$.items[1].quantity").value(this.item2.quantity()))
				.andExpect(jsonPath("$.items[1].unitPrice").value(is(this.item2.unitPrice()), BigDecimal.class))
				.andExpect(jsonPath("$.shipment.carrier").value(this.shipment1.carrier()))
				.andExpect(jsonPath("$.shipment.trackingNumber").value(this.shipment1.trackingNumber().toString()))
				.andExpect(jsonPath("$.shipment.deliveryDate").value(this.shipment1.deliveryDate().toString()))
				.andExpect(jsonPath("$.date").value("2020-07-14T00:00:00+09:00"))
				.andExpect(jsonPath("$.status").value(this.order1.status().toString()))
		;
	}

	@Test
	void searchOrdersByCustomerId() throws Exception {
		given(this.orderMapper.findByCustomerId(this.customer1.id())).willReturn(List.of(this.order2, this.order1));
		this.mockMvc.perform(get("/orders")
				.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_order:read"), new SimpleGrantedAuthority("ROLE_TRUSTED_CLIENT")))
				.param("customerId", this.customer1.id()))
				.andExpect(status().isOk())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value(this.orderId2))
				.andExpect(jsonPath("$[0].customer.id").value(this.customer1.id()))
				.andExpect(jsonPath("$[0].customer.firstName").value(this.customer1.firstName()))
				.andExpect(jsonPath("$[0].customer.lastName").value(this.customer1.lastName()))
				.andExpect(jsonPath("$[0].customer.username").value(this.customer1.username()))
				.andExpect(jsonPath("$[0].address.number").value(this.address1.number()))
				.andExpect(jsonPath("$[0].address.street").value(this.address1.street()))
				.andExpect(jsonPath("$[0].address.city").value(this.address1.city()))
				.andExpect(jsonPath("$[0].address.postcode").value(this.address1.postcode()))
				.andExpect(jsonPath("$[0].address.country").value(this.address1.country()))
				.andExpect(jsonPath("$[0].card.longNum").value(this.card1.longNum()))
				.andExpect(jsonPath("$[0].card.expires").value(this.card1.expires().toString()))
				.andExpect(jsonPath("$[0].card.ccv").value(this.card1.ccv()))
				.andExpect(jsonPath("$[0].items.length()").value(1))
				.andExpect(jsonPath("$[0].items[0].itemId").value(this.item3.itemId()))
				.andExpect(jsonPath("$[0].items[0].quantity").value(this.item3.quantity()))
				.andExpect(jsonPath("$[0].items[0].unitPrice").value(is(this.item3.unitPrice()), BigDecimal.class))
				.andExpect(jsonPath("$[0].shipment.trackingNumber").value(this.shipment1.trackingNumber().toString()))
				.andExpect(jsonPath("$[0].shipment.deliveryDate").value(this.shipment1.deliveryDate().toString()))
				.andExpect(jsonPath("$[0].date").value("2020-07-15T00:00:00+09:00"))
				.andExpect(jsonPath("$[0].status").value(this.order2.status().toString()))
				.andExpect(jsonPath("$[1].id").value(this.orderId1))
				.andExpect(jsonPath("$[1].customer.id").value(this.customer1.id()))
				.andExpect(jsonPath("$[1].customer.firstName").value(this.customer1.firstName()))
				.andExpect(jsonPath("$[1].customer.lastName").value(this.customer1.lastName()))
				.andExpect(jsonPath("$[1].customer.username").value(this.customer1.username()))
				.andExpect(jsonPath("$[1].address.number").value(this.address1.number()))
				.andExpect(jsonPath("$[1].address.street").value(this.address1.street()))
				.andExpect(jsonPath("$[1].address.city").value(this.address1.city()))
				.andExpect(jsonPath("$[1].address.postcode").value(this.address1.postcode()))
				.andExpect(jsonPath("$[1].address.country").value(this.address1.country()))
				.andExpect(jsonPath("$[1].card.longNum").value(this.card1.longNum()))
				.andExpect(jsonPath("$[1].card.expires").value(this.card1.expires().toString()))
				.andExpect(jsonPath("$[1].card.ccv").value(this.card1.ccv()))
				.andExpect(jsonPath("$[1].items.length()").value(2))
				.andExpect(jsonPath("$[1].items[0].itemId").value(this.item1.itemId()))
				.andExpect(jsonPath("$[1].items[0].quantity").value(this.item1.quantity()))
				.andExpect(jsonPath("$[1].items[0].unitPrice").value(is(this.item1.unitPrice()), BigDecimal.class))
				.andExpect(jsonPath("$[1].items[1].itemId").value(this.item2.itemId()))
				.andExpect(jsonPath("$[1].items[1].quantity").value(this.item2.quantity()))
				.andExpect(jsonPath("$[1].items[1].unitPrice").value(is(this.item2.unitPrice()), BigDecimal.class))
				.andExpect(jsonPath("$[1].shipment.carrier").value(this.shipment1.carrier()))
				.andExpect(jsonPath("$[1].shipment.trackingNumber").value(this.shipment1.trackingNumber().toString()))
				.andExpect(jsonPath("$[1].shipment.deliveryDate").value(this.shipment1.deliveryDate().toString()))
				.andExpect(jsonPath("$[1].date").value("2020-07-14T00:00:00+09:00"))
				.andExpect(jsonPath("$[1].status").value(this.order1.status().toString()))
		;
	}
}

@Configuration
class Config {
	@Bean
	public CircuitBreakerRegistry circuitBreakerRegistry() {
		return CircuitBreakerRegistry.ofDefaults();
	}
}