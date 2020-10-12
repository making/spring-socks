package lol.maki.socks;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.security.oauth2.client.provider.ui.issuer-uri=https://uaa.run.pcfone.io/oauth/token",
		"spring.security.oauth2.client.provider.sock.issuer-uri=https://uaa.run.pcfone.io/oauth/token" })
class ShopUiApplicationTests {

	@Test
	void contextLoads() {
	}

}
