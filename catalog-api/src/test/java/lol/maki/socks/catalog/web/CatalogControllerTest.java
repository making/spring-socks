package lol.maki.socks.catalog.web;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lol.maki.socks.catalog.Sock;
import lol.maki.socks.catalog.SockMapper;
import lol.maki.socks.catalog.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static org.mockito.BDDMockito.given;

@WebMvcTest(properties = "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://uaa.run.pcfone.io/oauth/token", controllers = CatalogController.class)
class CatalogControllerTest {
	@Autowired
	MockMvc mockMvc;

	@MockBean
	SockMapper sockMapper;

	WebTestClient webTestClient;

	private final Sock sock1 = new Sock(
			UUID.fromString("57b8db2f-15fc-4164-bfaf-ad30b55ef7e8"),
			"demo1",
			"Demo1",
			BigDecimal.valueOf(10.0),
			10,
			List.of("/demo1_1.jpg", "/demo1_2.jpg"),
			List.of(Tag.valueOf("blue"), Tag.valueOf("red")));

	private final Sock sock2 = new Sock(
			UUID.fromString("bb4f7c35-67f9-4f0b-90c2-69cde0964fbd"),
			"demo2",
			"Demo2",
			BigDecimal.valueOf(20.0),
			20,
			List.of("/demo2_1.jpg", "/demo2_2.jpg"),
			List.of(Tag.valueOf("red")));

	@BeforeEach
	void setup() throws Exception {
		this.webTestClient = MockMvcWebTestClient.bindTo(this.mockMvc).build();
	}

	@Test
	@WithMockUser(authorities = "SCOPE_catalog:read")
	void getSock() throws Exception {
		given(this.sockMapper.findOne(this.sock1.sockId())).willReturn(Optional.of(this.sock1));
		final EntityExchangeResult<byte[]> result = this.webTestClient.get()
				.uri("/catalogue/{sockId}", "57b8db2f-15fc-4164-bfaf-ad30b55ef7e8")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.id").isEqualTo("57b8db2f-15fc-4164-bfaf-ad30b55ef7e8")
				.jsonPath("$.name").isEqualTo("demo1")
				.jsonPath("$.description").isEqualTo("Demo1")
				.jsonPath("$.count").isEqualTo(10)
				.jsonPath("$.price").isEqualTo(10.0)
				.jsonPath("$.imageUrl.length()").isEqualTo(2)
				.jsonPath("$.imageUrl[0]").isEqualTo("/demo1_1.jpg")
				.jsonPath("$.imageUrl[1]").isEqualTo("/demo1_2.jpg")
				.jsonPath("$.tag.length()").isEqualTo(2)
				.jsonPath("$.tag[0]").isEqualTo("blue")
				.jsonPath("$.tag[1]").isEqualTo("red")
				.returnResult();
		MockMvcWebTestClient.resultActionsFor(result)
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"));
	}


	@Test
	@WithMockUser(authorities = "SCOPE_catalog:read")
	void getSocks() throws Exception {
		given(this.sockMapper.findSocks(List.of(Tag.valueOf("red")), "price", 1, 10)).willReturn(List.of(this.sock1, this.sock2));
		final EntityExchangeResult<byte[]> result = this.webTestClient.get()
				.uri("/catalogue?tags=red")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.length()").isEqualTo(2)
				.jsonPath("$[0].id").isEqualTo("57b8db2f-15fc-4164-bfaf-ad30b55ef7e8")
				.jsonPath("$[0].name").isEqualTo("demo1")
				.jsonPath("$[0].description").isEqualTo("Demo1")
				.jsonPath("$[0].count").isEqualTo(10)
				.jsonPath("$[0].price").isEqualTo(10.0)
				.jsonPath("$[0].imageUrl.length()").isEqualTo(2)
				.jsonPath("$[0].imageUrl[0]").isEqualTo("/demo1_1.jpg")
				.jsonPath("$[0].imageUrl[1]").isEqualTo("/demo1_2.jpg")
				.jsonPath("$[0].tag.length()").isEqualTo(2)
				.jsonPath("$[0].tag[0]").isEqualTo("blue")
				.jsonPath("$[0].tag[1]").isEqualTo("red")
				.jsonPath("$[1].id").isEqualTo("bb4f7c35-67f9-4f0b-90c2-69cde0964fbd")
				.jsonPath("$[1].name").isEqualTo("demo2")
				.jsonPath("$[1].description").isEqualTo("Demo2")
				.jsonPath("$[1].count").isEqualTo(20)
				.jsonPath("$[1].price").isEqualTo(20.0)
				.jsonPath("$[1].imageUrl.length()").isEqualTo(2)
				.jsonPath("$[1].imageUrl[0]").isEqualTo("/demo2_1.jpg")
				.jsonPath("$[1].imageUrl[1]").isEqualTo("/demo2_2.jpg")
				.jsonPath("$[1].tag.length()").isEqualTo(1)
				.jsonPath("$[1].tag[0]").isEqualTo("red")
				.returnResult();
		MockMvcWebTestClient.resultActionsFor(result)
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"));
	}
}