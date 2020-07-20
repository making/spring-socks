package lol.maki.socks;

import java.io.UncheckedIOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lol.maki.socks.config.SockProps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestConstructor(autowireMode = AutowireMode.ALL)
class IntegrationTestsApplicationTests {
	final Logger log = LoggerFactory.getLogger(IntegrationTestsApplicationTests.class);

	final WebClient webClient;

	final SockProps sockProps;

	final ObjectMapper objectMapper;

	IntegrationTestsApplicationTests(Builder builder, SockProps sockProps, ObjectMapper objectMapper) {
		this.webClient = WebClient.builder()
				.exchangeStrategies(ExchangeStrategies.builder().codecs(c -> c.defaultCodecs().enableLoggingRequestDetails(true)).build())
				.build();
		this.sockProps = sockProps;
		this.objectMapper = objectMapper;
	}

	@BeforeEach
	void reset() {
		this.webClient.delete()
				.uri(this.sockProps.getFrontendUrl(), b -> b.path("cart").build())
				.exchange()
				.block();
	}

	@Test
	void contextLoads() {
		// Get items
		final ResponseEntity<JsonNode> catalogue = this.webClient.get()
				.uri(this.sockProps.getFrontendUrl(), b -> b.path("catalogue").build())
				.exchange()
				.flatMap(x -> x.toEntity(JsonNode.class))
				.block();
		final JsonNode item1 = catalogue.getBody().get(0);
		final JsonNode item2 = catalogue.getBody().get(1);
		log.info("item1 = {}", prettyPrint(item1));
		log.info("item2 = {}", prettyPrint(item2));
		assertThat(catalogue.getStatusCode().is2xxSuccessful()).isTrue();

		// Add items to a cart
		final ResponseEntity<JsonNode> cartItem1 = this.webClient.post()
				.uri(this.sockProps.getFrontendUrl(), b -> b.path("cart").build())
				.bodyValue(Map.of("id", item1.get("id").asText()))
				.exchange()
				.flatMap(x -> x.toEntity(JsonNode.class))
				.block();
		final ResponseEntity<JsonNode> cartItem2 = this.webClient.post()
				.uri(this.sockProps.getFrontendUrl(), b -> b.path("cart").build(this.sockProps.getTestCustomerId()))
				.bodyValue(Map.of("id", item2.get("id").asText()))
				.exchange()
				.flatMap(x -> x.toEntity(JsonNode.class))
				.block();
		log.info("cartItem1 = {}", prettyPrint(cartItem1.getBody()));
		log.info("cartItem2 = {}", prettyPrint(cartItem2.getBody()));
		assertThat(cartItem1.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(cartItem2.getStatusCode().is2xxSuccessful()).isTrue();

		// Increase the quantity of the cart item
		final ResponseEntity<JsonNode> updatedCartItem2 = this.webClient.post()
				.uri(this.sockProps.getFrontendUrl(), b -> b.path("cart/update").build())
				.bodyValue(Map.of(
						"id", item2.get("id").asText(),
						"quantity", 3
				))
				.exchange()
				.flatMap(x -> x.toEntity(JsonNode.class))
				.block();
		log.info("cartItem2 = {}", prettyPrint(updatedCartItem2.getBody()));
		assertThat(updatedCartItem2.getStatusCode().is2xxSuccessful()).isTrue();

		// Show a cart
		final ResponseEntity<JsonNode> cart = this.webClient.get()
				.uri(this.sockProps.getFrontendUrl(), b -> b.path("cart").build())
				.exchange()
				.flatMap(x -> x.toEntity(JsonNode.class))
				.block();
		log.info("cart = {}", prettyPrint(cart.getBody()));
		assertThat(cart.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(cart.getBody()).hasSize(2);

		// Place a new order
		final ResponseEntity<JsonNode> order = this.webClient.post()
				.uri(this.sockProps.getFrontendUrl(), b -> b.path("orders").build())
				.exchange()
				.flatMap(x -> x.toEntity(JsonNode.class))
				.block();
		log.info("order = {}", prettyPrint(order.getBody()));
		assertThat(order.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(order.getBody().get("total").asText()).isEqualTo("40.96");
		assertThat(order.getBody().get("status").asText()).isEqualTo("CREATED");
		assertThat(order.getBody().get("shipment").get("carrier").asText()).isEqualTo("USPS");

		// Show a shipment
		final ResponseEntity<JsonNode> shipment = this.webClient.get()
				.uri(this.sockProps.getFrontendUrl(), b -> b.path("shipping/{orderId}").build(order.getBody().get("id").asText()))
				.exchange()
				.flatMap(x -> x.toEntity(JsonNode.class))
				.block();
		log.info("shipment = {}", prettyPrint(shipment.getBody()));
		assertThat(shipment.getStatusCode().is2xxSuccessful()).isTrue();

		// Show a cart again
		final ResponseEntity<JsonNode> cartAgain = this.webClient.get()
				.uri(this.sockProps.getFrontendUrl(), b -> b.path("cart").build(this.sockProps.getTestCustomerId()))
				.exchange()
				.flatMap(x -> x.toEntity(JsonNode.class))
				.block();
		log.info("cart = {}", prettyPrint(cartAgain.getBody()));
		assertThat(cartAgain.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(cartAgain.getBody()).hasSize(0);
	}

	String prettyPrint(JsonNode json) {
		try {
			return System.lineSeparator() + this.objectMapper.writeValueAsString(json);
		}
		catch (JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
	}
}
