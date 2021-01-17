package lol.maki.socks.shipping.web;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lol.maki.socks.shipping.Carrier;
import lol.maki.socks.shipping.Shipment;
import lol.maki.socks.shipping.ShipmentMapper;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.IdGenerator;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(properties = "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://uaa.run.pcfone.io/oauth/token", controllers = ShippingController.class)
class ShippingControllerTest {
	@Autowired
	MockMvc mockMvc;

	@MockBean
	ShipmentMapper shipmentMapper;

	@MockBean
	Clock clock;

	@MockBean
	IdGenerator idGenerator;

	Shipment shipment1 = new Shipment(
			Carrier.FEDEX,
			"1",
			LocalDate.of(2020, 7, 1),
			UUID.fromString("6a8cd59f-73dd-40a1-b2ca-ec53f78e391a"));

	Shipment shipment2 = new Shipment(
			Carrier.UPS,
			"2",
			LocalDate.of(2020, 7, 2),
			UUID.fromString("a22f048f-4617-46f2-b1bb-93885d66daf7"));

	@Test
	void getShippingById() throws Exception {
		given(this.shipmentMapper.findByOrderId("1")).willReturn(Optional.of(this.shipment1));

		this.mockMvc.perform(get("/shipping/{orderId}", "1")
				.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_shipping:read"))))
				.andExpect(status().isOk())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
				.andExpect(jsonPath("$.carrier").value("FEDEX"))
				.andExpect(jsonPath("$.deliveryDate").value("2020-07-02"))
				.andExpect(jsonPath("$.orderId").value("1"))
				.andExpect(jsonPath("$.trackingNumber").value("6a8cd59f-73dd-40a1-b2ca-ec53f78e391a"));
	}

	@Test
	void getShippings() throws Exception {
		given(this.shipmentMapper.findAll()).willReturn(List.of(this.shipment1, this.shipment2));

		this.mockMvc.perform(get("/shipping")
				.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_shipping:read"))))
				.andExpect(status().isOk())
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].carrier").value("FEDEX"))
				.andExpect(jsonPath("$[0].deliveryDate").value("2020-07-02"))
				.andExpect(jsonPath("$[0].orderId").value("1"))
				.andExpect(jsonPath("$[0].trackingNumber").value("6a8cd59f-73dd-40a1-b2ca-ec53f78e391a"))
				.andExpect(jsonPath("$[1].carrier").value("UPS"))
				.andExpect(jsonPath("$[1].deliveryDate").value("2020-07-05"))
				.andExpect(jsonPath("$[1].orderId").value("2"))
				.andExpect(jsonPath("$[1].trackingNumber").value("a22f048f-4617-46f2-b1bb-93885d66daf7"));
	}

	@Test
	void postShipping() throws Exception {
		given(this.shipmentMapper.insert(any())).willReturn(1);
		given(this.clock.instant()).willReturn(LocalDate.of(2020, 7, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
		given(this.clock.getZone()).willReturn(ZoneId.systemDefault());
		given(this.idGenerator.generateId()).willReturn(UUID.fromString("7e8992a3-8492-482b-9432-f28428db6472"));

		this.mockMvc.perform(post("/shipping")
				.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_shipping:write")))
				.contentType(APPLICATION_JSON)
				.characterEncoding(UTF_8.name())
				.content("{\"orderId\": \"3\", \"itemCount\": 5}"))
				.andExpect(status().isCreated())
				.andExpect(header().string(LOCATION, "http://localhost/shipping/3"))
				.andExpect(openApi().isValid("META-INF/resources/openapi/doc.yml"))
				.andExpect(jsonPath("$.carrier").value("USPS"))
				.andExpect(jsonPath("$.deliveryDate").value("2020-07-06"))
				.andExpect(jsonPath("$.orderId").value("3"))
				.andExpect(jsonPath("$.trackingNumber").value("7e8992a3-8492-482b-9432-f28428db6472"));
	}
}