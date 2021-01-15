package lol.maki.socks;

import java.io.UncheckedIOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lol.maki.socks.config.LoggingExchangeFilterFunction;
import lol.maki.socks.config.SockProps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestConstructor(autowireMode = AutowireMode.ALL)
class IntegrationTestsApplicationTests {
	final Logger log = LoggerFactory.getLogger(IntegrationTestsApplicationTests.class);

	final WebClient webClient;

	final SockProps sockProps;

	final ObjectMapper objectMapper;

	String userAccessToken;

	JsonNode userInfo;

	String clientAccessToken;

	IntegrationTestsApplicationTests(SockProps sockProps, ObjectMapper objectMapper) {
		this.webClient = WebClient.builder()
				.filter(new LoggingExchangeFilterFunction(true))
				.build();
		this.sockProps = sockProps;
		this.objectMapper = objectMapper;
	}

	@BeforeEach
	void reset() throws Exception {
		this.userAccessToken = this.webClient.post()
				.uri(this.sockProps.getUserUrl(), b -> b.path("oauth/token").build())
				.headers(httpHeaders -> httpHeaders.setBasicAuth(this.sockProps.getClientId(), this.sockProps.getClientSecret()))
				.bodyValue(new LinkedMultiValueMap<>() {
					{
						add("grant_type", "password");
						add("username", sockProps.getUsername());
						add("password", sockProps.getPassword());
					}
				})
				.retrieve()
				.bodyToMono(JsonNode.class)
				.map(n -> n.get("access_token").asText())
				.block();
		this.clientAccessToken = this.webClient.post()
				.uri(this.sockProps.getUserUrl(), b -> b.path("oauth/token").build())
				.headers(httpHeaders -> httpHeaders.setBasicAuth(this.sockProps.getClientId(), this.sockProps.getClientSecret()))
				.bodyValue(new LinkedMultiValueMap<>() {
					{
						add("grant_type", "client_credentials");
					}
				})
				.retrieve()
				.bodyToMono(JsonNode.class)
				.map(n -> n.get("access_token").asText())
				.block();
		this.userInfo = this.objectMapper.readValue(Base64Utils.decodeFromString(this.userAccessToken.split("\\.")[1]), JsonNode.class);
		final ResponseEntity<Void> response = this.webClient.delete()
				.uri(this.sockProps.getCartUrl(), b -> b.path("carts/{customerId}").build(this.userInfo.get("sub").asText()))
				.headers(httpHeaders -> httpHeaders.setBearerAuth(this.clientAccessToken))
				.exchangeToMono(ClientResponse::toBodilessEntity)
				.block();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
	}

	@Test
	void contextLoads() {
		// Get items
		final ResponseEntity<JsonNode> catalogue = this.webClient.get()
				.uri(this.sockProps.getCatalogUrl(), b -> b.path("catalogue").build())
				.headers(httpHeaders -> httpHeaders.setBearerAuth(this.userAccessToken))
				.exchangeToMono(x -> x.toEntity(JsonNode.class))
				.block();
		assertThat(catalogue.getStatusCode()).isEqualTo(HttpStatus.OK);
		final JsonNode item1 = catalogue.getBody().get(0);
		final JsonNode item2 = catalogue.getBody().get(1);
		// Add items to a cart
		final ResponseEntity<JsonNode> cartItem1 = this.webClient.post()
				.uri(this.sockProps.getCartUrl(), b -> b.path("carts/{customerId}/items").build(this.userInfo.get("sub").asText()))
				.headers(httpHeaders -> httpHeaders.setBearerAuth(this.clientAccessToken))
				.bodyValue(Map.of("itemId", item1.get("id").asText(),
						"quantity", 1,
						"unitPrice", item1.get("price").asInt()))
				.exchangeToMono(x -> x.toEntity(JsonNode.class))
				.block();
		final ResponseEntity<JsonNode> cartItem2 = this.webClient.post()
				.uri(this.sockProps.getCartUrl(), b -> b.path("carts/{customerId}/items").build(this.userInfo.get("sub").asText()))
				.headers(httpHeaders -> httpHeaders.setBearerAuth(this.clientAccessToken))
				.bodyValue(Map.of("itemId", item2.get("id").asText(),
						"quantity", 1,
						"unitPrice", item2.get("price").asInt())).exchangeToMono(x -> x.toEntity(JsonNode.class))
				.block();
		assertThat(cartItem1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(cartItem2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		// Increase the quantity of the cart item
		final ResponseEntity<JsonNode> updatedCartItem2 = this.webClient.patch()
				.uri(this.sockProps.getCartUrl(), b -> b.path("carts/{customerId}/items").build(this.userInfo.get("sub").asText()))
				.headers(httpHeaders -> httpHeaders.setBearerAuth(this.clientAccessToken))
				.bodyValue(Map.of(
						"itemId", item2.get("id").asText(),
						"quantity", 3,
						"unitPrice", item2.get("price").asInt()
				))
				.exchangeToMono(x -> x.toEntity(JsonNode.class))
				.block();
		assertThat(updatedCartItem2.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
		// Show a cart
		final ResponseEntity<JsonNode> cart = this.webClient.get()
				.uri(this.sockProps.getCartUrl(), b -> b.path("carts/{customerId}").build(this.userInfo.get("sub").asText()))
				.headers(httpHeaders -> httpHeaders.setBearerAuth(this.clientAccessToken))
				.exchangeToMono(x -> x.toEntity(JsonNode.class))
				.block();
		assertThat(cart.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(cart.getBody()).hasSize(2);
		assertThat(cart.getBody().get("items").get(0).get("quantity").asInt()).isEqualTo(1);
		assertThat(cart.getBody().get("items").get(1).get("quantity").asInt()).isEqualTo(3);
		// Get me
		final ResponseEntity<JsonNode> me = this.webClient.get()
				.uri(this.sockProps.getUserUrl(), b -> b.path("me").build())
				.headers(httpHeaders -> httpHeaders.setBearerAuth(this.userAccessToken))
				.exchangeToMono(x -> x.toEntity(JsonNode.class))
				.block();
		assertThat(me.getStatusCode()).isEqualTo(HttpStatus.OK);
		final String addressId = me.getBody().get("addresses").get(0).get("addressId").asText();
		final String cartId = me.getBody().get("cards").get(0).get("cardId").asText();
		// Place a new order
		final ResponseEntity<JsonNode> order = this.webClient.post()
				.uri(this.sockProps.getOrderUrl(), b -> b.path("orders").build())
				.bodyValue(Map.of(
						"addressId", addressId,
						"cardId", cartId))
				.headers(httpHeaders -> httpHeaders.setBearerAuth(this.userAccessToken))
				.exchangeToMono(x -> x.toEntity(JsonNode.class))
				.block();
		assertThat(order.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(order.getBody().get("total").asText()).isEqualTo("37.0");
		assertThat(order.getBody().get("status").asText()).isEqualTo("CREATED");
		assertThat(order.getBody().get("shipment").get("carrier").asText()).isEqualTo("USPS");

		// Show a shipment
		final ResponseEntity<JsonNode> shipment = this.webClient.get()
				.uri(this.sockProps.getShippingUrl(), b -> b.path("shipping/{orderId}").build(order.getBody().get("id").asText()))
				.headers(httpHeaders -> httpHeaders.setBearerAuth(this.userAccessToken))
				.exchangeToMono(x -> x.toEntity(JsonNode.class))
				.block();
		assertThat(shipment.getStatusCode()).isEqualTo(HttpStatus.OK);

		// Show a cart again
		final ResponseEntity<JsonNode> cartAgain = this.webClient.get()
				.uri(this.sockProps.getCartUrl(), b -> b.path("carts/{customerId}").build(this.userInfo.get("sub").asText()))
				.headers(httpHeaders -> httpHeaders.setBearerAuth(this.clientAccessToken))
				.exchangeToMono(x -> x.toEntity(JsonNode.class))
				.block();
		assertThat(cartAgain.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(cartAgain.getBody().get("items")).hasSize(0);
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
