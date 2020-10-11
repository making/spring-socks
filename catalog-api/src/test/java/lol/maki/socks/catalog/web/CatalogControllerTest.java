package lol.maki.socks.catalog.web;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lol.maki.socks.catalog.ImmutableSock;
import lol.maki.socks.catalog.Sock;
import lol.maki.socks.catalog.SockMapper;
import lol.maki.socks.catalog.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalogController.class)
class CatalogControllerTest {
	@Autowired
	MockMvc mockMvc;

	@MockBean
	SockMapper sockMapper;

	private final Sock sock1 = ImmutableSock.builder()
			.sockId(UUID.fromString("57b8db2f-15fc-4164-bfaf-ad30b55ef7e8"))
			.name("demo1")
			.description("Demo1")
			.addTags(Tag.valueOf("blue"), Tag.valueOf("red"))
			.imageUrl(List.of("/demo1_1.jpg", "/demo1_2.jpg"))
			.price(BigDecimal.valueOf(10.0))
			.count(10)
			.build();

	private final Sock sock2 = ImmutableSock.builder()
			.sockId(UUID.fromString("bb4f7c35-67f9-4f0b-90c2-69cde0964fbd"))
			.name("demo2")
			.description("Demo2")
			.addTags(Tag.valueOf("red"))
			.imageUrl(List.of("/demo2_1.jpg", "/demo2_2.jpg"))
			.price(BigDecimal.valueOf(20.0))
			.count(20)
			.build();

	@Test
	void getSock() throws Exception {
		given(this.sockMapper.findOne(this.sock1.sockId())).willReturn(Optional.of(this.sock1));
		this.mockMvc.perform(get("/catalogue/{sockId}", "57b8db2f-15fc-4164-bfaf-ad30b55ef7e8")
				.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_catalog:read"))))
				.andExpect(status().isOk())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
				.andExpect(jsonPath("$.id").value("57b8db2f-15fc-4164-bfaf-ad30b55ef7e8"))
				.andExpect(jsonPath("$.name").value("demo1"))
				.andExpect(jsonPath("$.description").value("Demo1"))
				.andExpect(jsonPath("$.count").value(10))
				.andExpect(jsonPath("$.price").value(10.0))
				.andExpect(jsonPath("$.imageUrl.length()").value(2))
				.andExpect(jsonPath("$.imageUrl[0]").value("/demo1_1.jpg"))
				.andExpect(jsonPath("$.imageUrl[1]").value("/demo1_2.jpg"))
				.andExpect(jsonPath("$.tag.length()").value(2))
				.andExpect(jsonPath("$.tag[0]").value("blue"))
				.andExpect(jsonPath("$.tag[1]").value("red"));
	}


	@Test
	void getSocks() throws Exception {
		given(this.sockMapper.findSocks(List.of(Tag.valueOf("red")), "price", 1, 10)).willReturn(List.of(this.sock1, this.sock2));
		this.mockMvc.perform(get("/catalogue").param("tags", "red")
				.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_catalog:read"))))
				.andExpect(status().isOk())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value("57b8db2f-15fc-4164-bfaf-ad30b55ef7e8"))
				.andExpect(jsonPath("$[0].name").value("demo1"))
				.andExpect(jsonPath("$[0].description").value("Demo1"))
				.andExpect(jsonPath("$[0].count").value(10))
				.andExpect(jsonPath("$[0].price").value(10.0))
				.andExpect(jsonPath("$[0].imageUrl.length()").value(2))
				.andExpect(jsonPath("$[0].imageUrl[0]").value("/demo1_1.jpg"))
				.andExpect(jsonPath("$[0].imageUrl[1]").value("/demo1_2.jpg"))
				.andExpect(jsonPath("$[0].tag.length()").value(2))
				.andExpect(jsonPath("$[0].tag[0]").value("blue"))
				.andExpect(jsonPath("$[0].tag[1]").value("red"))
				.andExpect(jsonPath("$[1].id").value("bb4f7c35-67f9-4f0b-90c2-69cde0964fbd"))
				.andExpect(jsonPath("$[1].name").value("demo2"))
				.andExpect(jsonPath("$[1].description").value("Demo2"))
				.andExpect(jsonPath("$[1].count").value(20))
				.andExpect(jsonPath("$[1].price").value(20.0))
				.andExpect(jsonPath("$[1].imageUrl.length()").value(2))
				.andExpect(jsonPath("$[1].imageUrl[0]").value("/demo2_1.jpg"))
				.andExpect(jsonPath("$[1].imageUrl[1]").value("/demo2_2.jpg"))
				.andExpect(jsonPath("$[1].tag.length()").value(1))
				.andExpect(jsonPath("$[1].tag[0]").value("red"));
	}
}