package lol.maki.socks.payment.web;

import lol.maki.socks.config.PaymentProps;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@EnableConfigurationProperties(PaymentProps.class)
class PaymentControllerTest {
	@Autowired
	MockMvc mockMvc;

	@Test
	void authorizePayment() throws Exception {
		this.mockMvc.perform(post("/paymentAuth")
				.contentType(APPLICATION_JSON)
				.characterEncoding(UTF_8.name())
				.content("{\"amount\": 105.0}"))
				.andExpect(status().isOk())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
				.andExpect(jsonPath("$.authorization.authorised").value(true))
				.andExpect(jsonPath("$.authorization.message").value("Payment authorised"));
	}

	@Test
	void invalidPayment() throws Exception {
		this.mockMvc.perform(post("/paymentAuth")
				.contentType(APPLICATION_JSON)
				.characterEncoding(UTF_8.name())
				.content("{\"amount\": 0.0}"))
				.andExpect(status().isBadRequest())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
				.andExpect(jsonPath("$.authorization.authorised").value(false))
				.andExpect(jsonPath("$.authorization.message").value("Invalid payment amount"));
	}

	@Test
	void declinedPayment() throws Exception {
		this.mockMvc.perform(post("/paymentAuth")
				.contentType(APPLICATION_JSON)
				.characterEncoding(UTF_8.name())
				.content("{\"amount\": 105.1}"))
				.andExpect(status().isUnauthorized())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
				.andExpect(jsonPath("$.authorization.authorised").value(false))
				.andExpect(jsonPath("$.authorization.message").value("Payment declined: amount exceeds 105.00"));
	}
}