package lol.maki.socks.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class ShopUserImpl implements ShopUser, Serializable {
	private static final long serialVersionUID = 1L;

	private final OidcUser user;

	private final OAuth2AccessToken accessToken;

	public ShopUserImpl(OidcUser user, OAuth2AccessToken accessToken) {
		this.user = user;
		this.accessToken = accessToken;
	}

	@Override
	public OAuth2AccessToken getAccessToken() {
		return this.accessToken;
	}

	@Override
	public Map<String, Object> getClaims() {
		return this.user.getClaims();
	}

	@Override
	public OidcUserInfo getUserInfo() {
		return this.user.getUserInfo();
	}

	@Override
	public OidcIdToken getIdToken() {
		return this.user.getIdToken();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return this.user.getAttributes();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.user.getAuthorities();
	}

	@Override
	public String getName() {
		return this.user.getName();
	}
}
