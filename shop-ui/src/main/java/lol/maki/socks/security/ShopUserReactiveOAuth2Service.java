package lol.maki.socks.security;

import java.util.Collection;
import java.util.Map;

import reactor.core.publisher.Mono;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
public class ShopUserReactiveOAuth2Service implements ReactiveOAuth2UserService<OidcUserRequest, OidcUser> {
	private final OidcReactiveOAuth2UserService delegate = new OidcReactiveOAuth2UserService();

	@Override
	public Mono<OidcUser> loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
		return this.delegate.loadUser(userRequest)
				.map(user -> new ShopUser() {
					@Override
					public OAuth2AccessToken getAccessToken() {
						return userRequest.getAccessToken();
					}

					@Override
					public Map<String, Object> getClaims() {
						return user.getClaims();
					}

					@Override
					public OidcUserInfo getUserInfo() {
						return user.getUserInfo();
					}

					@Override
					public OidcIdToken getIdToken() {
						return user.getIdToken();
					}

					@Override
					public Map<String, Object> getAttributes() {
						return user.getAttributes();
					}

					@Override
					public Collection<? extends GrantedAuthority> getAuthorities() {
						return user.getAuthorities();
					}

					@Override
					public String getName() {
						return user.getName();
					}
				});
	}

}
