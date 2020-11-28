package lol.maki.socks.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "sock")
@Component
public class SockProps {
	private String homeUrl;

	private String loginUrl;

	private String issuerUrl;

	public void setHomeUrl(String homeUrl) {
		this.homeUrl = homeUrl;
	}

	public String getHomeUrl() {
		return this.homeUrl;
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public String getLoginUrl() {
		return loginUrl;
	}

	public String getIssuerUrl() {
		return issuerUrl;
	}

	public void setIssuerUrl(String issuerUrl) {
		this.issuerUrl = issuerUrl;
	}
}
