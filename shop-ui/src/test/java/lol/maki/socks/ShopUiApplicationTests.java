package lol.maki.socks;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(properties = {
		"server.port=34561",
		"spring.security.oauth2.client.provider.ui.issuer-uri=https://uaa.run.pcfone.io/oauth/token",
		"spring.security.oauth2.client.provider.sock.issuer-uri=https://uaa.run.pcfone.io/oauth/token" },
		webEnvironment = WebEnvironment.DEFINED_PORT)
class ShopUiApplicationTests {

	@Test
	void contextLoads() {
	}

}
