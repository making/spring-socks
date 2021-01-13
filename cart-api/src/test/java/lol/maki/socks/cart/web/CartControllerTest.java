package lol.maki.socks.cart.web;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;

import lol.maki.socks.cart.Cart;
import lol.maki.socks.cart.CartItem;
import lol.maki.socks.cart.CartMapper;
import lol.maki.socks.cart.CartService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(properties = "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://uaa.run.pcfone.io/oauth/token", controllers = CartController.class)
@Import(CartService.class)
class CartControllerTest {
	@Autowired
	MockMvc mockMvc;

	@MockBean
	CartMapper cartMapper;

	@Autowired
	CartService cartService;

	CartItem cartItem1 = new CartItem("1", 1, BigDecimal.TEN);

	CartItem cartItem2 = new CartItem("2", 2, BigDecimal.ONE);

	CartItem cartItem3 = new CartItem("2", 1, BigDecimal.ONE);

	CartItem cartItem4 = new CartItem("3", 1, BigDecimal.TEN);

	Cart cart1 = new Cart("1234", new ArrayList<>() {
		{
			add(cartItem1);
			add(cartItem2);
		}
	});

	Cart cart2 = new Cart("5678", new ArrayList<>() {
		{
			add(cartItem3);
			add(cartItem4);
		}
	});

	@Test
	void deleteCartByCustomerId() throws Exception {
		given(this.cartMapper.deleteCartByCustomerId("1234")).willReturn(1);
		this.mockMvc.perform(delete("/carts/{customerId}", "1234")
				.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_cart:write"))))
				.andExpect(status().isAccepted())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
		;
	}

	@Test
	void deleteCartItemByCartIdAndItemId() throws Exception {
		assertThat(this.cart1.findItem("2").isPresent()).isTrue();
		given(this.cartMapper.findCartByCustomerId("1234")).willReturn(Optional.of(this.cart1));
		given(this.cartMapper.deleteCartByCustomerIdAndItemId("1234", "2")).willReturn(1);
		this.mockMvc.perform(delete("/carts/{customerId}/items/{itemId}", "1234", "2")
				.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_cart:write"))))
				.andExpect(status().isAccepted())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
		;
		assertThat(this.cart1.findItem("2").isPresent()).isFalse();
	}

	@Test
	void getCartByCustomerId() throws Exception {
		given(this.cartMapper.findCartByCustomerId("1234")).willReturn(Optional.of(this.cart1));
		this.mockMvc.perform(get("/carts/{customerId}", "1234")
				.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_cart:read"))))
				.andExpect(status().isOk())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
				.andExpect(jsonPath("$.customerId").value(this.cart1.customerId()))
				.andExpect(jsonPath("$.items.length()").value(2))
				.andExpect(jsonPath("$.items[0].itemId").value("1"))
				.andExpect(jsonPath("$.items[0].quantity").value(1))
				.andExpect(jsonPath("$.items[0].unitPrice").value(10))
				.andExpect(jsonPath("$.items[1].itemId").value("2"))
				.andExpect(jsonPath("$.items[1].quantity").value(2))
				.andExpect(jsonPath("$.items[1].unitPrice").value(1))
		;
	}

	@Test
	void getCartItemByCartIdAndItemId() throws Exception {
		given(this.cartMapper.findCartItemByCustomerIdAndItemId("1234", "1")).willReturn(Optional.of(this.cartItem1));
		this.mockMvc.perform(get("/carts/{customerId}/items/{itemId}", "1234", "1")
				.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_cart:read"))))
				.andExpect(status().isOk())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
				.andExpect(jsonPath("$.itemId").value("1"))
				.andExpect(jsonPath("$.quantity").value(1))
				.andExpect(jsonPath("$.unitPrice").value(10))
		;
	}

	@Test
	void getItemsByCustomerId() throws Exception {
		given(this.cartMapper.findCartByCustomerId("1234")).willReturn(Optional.of(this.cart1));
		this.mockMvc.perform(get("/carts/{customerId}/items", "1234")
				.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_cart:read"))))
				.andExpect(status().isOk())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].itemId").value("1"))
				.andExpect(jsonPath("$[0].quantity").value(1))
				.andExpect(jsonPath("$[0].unitPrice").value(10))
				.andExpect(jsonPath("$[1].itemId").value("2"))
				.andExpect(jsonPath("$[1].quantity").value(2))
				.andExpect(jsonPath("$[1].unitPrice").value(1))
		;
	}

	@Test
	void mergeCartsByCustomerId() throws Exception {
		given(this.cartMapper.findCartByCustomerId("1234")).willReturn(Optional.of(this.cart1));
		given(this.cartMapper.findCartByCustomerId("5678")).willReturn(Optional.of(this.cart2));
		this.mockMvc.perform(get("/carts/{customerId}/merge", "1234")
				.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_cart:write"),
						new SimpleGrantedAuthority("ROLE_TRUSTED_CLIENT")))
				.param("sessionId", "5678"))
				.andExpect(status().isAccepted())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
		;
		assertThat(this.cart1.items()).hasSize(3);
		assertThat(this.cart1.items()).containsExactly(this.cartItem1, new CartItem("2", 3, BigDecimal.ONE), this.cartItem4);
	}

	@Test
	void patchItemsByCustomerId() throws Exception {
		assertThat(this.cart1.findItem("1").isPresent()).isTrue();
		assertThat(this.cart1.findItem("1").get().quantity()).isEqualTo(1);
		given(this.cartMapper.findCartByCustomerId("1234")).willReturn(Optional.of(this.cart1));
		this.mockMvc.perform(patch("/carts/{customerId}/items", "1234")
				.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_cart:write"),
						new SimpleGrantedAuthority("ROLE_TRUSTED_CLIENT")))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8.toString())
				.content("{\"itemId\":\"1\",\"quantity\":2}"))
				.andExpect(status().isAccepted())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
		;
		assertThat(this.cart1.findItem("1").isPresent()).isTrue();
		assertThat(this.cart1.findItem("1").get().quantity()).isEqualTo(2);
	}

	@Test
	void patchItemsByCustomerId_400() throws Exception {
		assertThat(this.cart1.findItem("1").isPresent()).isTrue();
		assertThat(this.cart1.findItem("1").get().quantity()).isEqualTo(1);
		given(this.cartMapper.findCartByCustomerId("1234")).willReturn(Optional.of(this.cart1));
		this.mockMvc.perform(patch("/carts/{customerId}/items", "1234")
				.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_cart:write"),
						new SimpleGrantedAuthority("ROLE_TRUSTED_CLIENT")))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8.toString())
				.content("{\"itemId\":\"\",\"quantity\":-1}"))
				.andExpect(status().isBadRequest())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
				.andExpect(jsonPath("$.details.length()").value(2))
				.andExpect(jsonPath("$.details[0].defaultMessage").value("\"itemId\" must not be blank"))
				.andExpect(jsonPath("$.details[1].defaultMessage").value("\"quantity\" must be greater than or equal to 1"))
		;
		assertThat(this.cart1.findItem("1").get().quantity()).isEqualTo(1);
	}

	@Test
	void postItemsByCustomerId() throws Exception {
		assertThat(this.cart1.findItem("3").isPresent()).isFalse();
		given(this.cartMapper.findCartByCustomerId("1234")).willReturn(Optional.of(this.cart1));
		this.mockMvc.perform(post("/carts/{customerId}/items", "1234")
				.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_cart:write"),
						new SimpleGrantedAuthority("ROLE_TRUSTED_CLIENT")))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8.toString())
				.content("{\"itemId\":\"3\",\"quantity\":10,\"unitPrice\":1}"))
				.andExpect(status().isCreated())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
		;
		assertThat(this.cart1.findItem("3").isPresent()).isTrue();
		final CartItem cartItem = this.cart1.findItem("3").get();
		assertThat(cartItem.itemId()).isEqualTo("3");
		assertThat(cartItem.unitPrice()).isEqualTo(BigDecimal.ONE);
		assertThat(cartItem.quantity()).isEqualTo(10);
	}

	@Test
	void postItemsByCustomerId_400() throws Exception {
		assertThat(this.cart1.findItem("3").isPresent()).isFalse();
		given(this.cartMapper.findCartByCustomerId("1234")).willReturn(Optional.of(this.cart1));
		this.mockMvc.perform(post("/carts/{customerId}/items", "1234")
				.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_cart:write"),
						new SimpleGrantedAuthority("ROLE_TRUSTED_CLIENT")))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8.toString())
				.content("{\"itemId\":\"\",\"quantity\":0,\"unitPrice\":0}"))
				.andExpect(status().isBadRequest())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
				.andExpect(jsonPath("$.details.length()").value(3))
				.andExpect(jsonPath("$.details[0].defaultMessage").value("\"itemId\" must not be blank"))
				.andExpect(jsonPath("$.details[1].defaultMessage").value("\"quantity\" must be greater than or equal to 1"))
				.andExpect(jsonPath("$.details[2].defaultMessage").value("\"unitPrice\" must be greater than 0"))
		;
		assertThat(this.cart1.findItem("3").isPresent()).isFalse();
	}
}