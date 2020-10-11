package lol.maki.socks.catalog.web;

import java.util.List;

import lol.maki.socks.catalog.Tag;
import lol.maki.socks.catalog.TagMapper;
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

@WebMvcTest(properties = "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://uaa.run.pcfone.io/oauth/token", controllers = TagController.class)
class TagControllerTest {
	@Autowired
	MockMvc mockMvc;

	@MockBean
	TagMapper tagMapper;

	private final Tag tag1 = Tag.valueOf("red");

	private final Tag tag2 = Tag.valueOf("blue");

	private final Tag tag3 = Tag.valueOf("green");


	@Test
	void getTags() throws Exception {
		given(this.tagMapper.findAll()).willReturn(List.of(this.tag1, this.tag2, this.tag3));
		this.mockMvc.perform(get("/tags")
				.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_catalog:read"))))
				.andExpect(status().isOk())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
				.andExpect(jsonPath("$.tags.length()").value(3))
				.andExpect(jsonPath("$.tags[0]").value("red"))
				.andExpect(jsonPath("$.tags[1]").value("blue"))
				.andExpect(jsonPath("$.tags[2]").value("green"));
	}
}