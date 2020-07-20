package lol.maki.socks.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "sock")
@ConstructorBinding
public class SockProps {
	private final String testCustomerId;

	private final String frontendUrl;

	public SockProps(String testCustomerId, String frontendUrl) {
		this.testCustomerId = testCustomerId;
		this.frontendUrl = frontendUrl;
	}

	public String getTestCustomerId() {
		return testCustomerId;
	}

	public String getFrontendUrl() {
		return frontendUrl;
	}
}
