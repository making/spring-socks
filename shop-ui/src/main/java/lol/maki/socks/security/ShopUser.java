package lol.maki.socks.security;

import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface ShopUser extends OidcUser {
	OAuth2AccessToken getAccessToken();

	default String customerId() {
		return this.getSubject();
	}
}
