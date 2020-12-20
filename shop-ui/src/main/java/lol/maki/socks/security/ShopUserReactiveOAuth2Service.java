package lol.maki.socks.security;

import reactor.core.publisher.Mono;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
public class ShopUserReactiveOAuth2Service implements ReactiveOAuth2UserService<OidcUserRequest, OidcUser> {
	private final OidcReactiveOAuth2UserService delegate = new OidcReactiveOAuth2UserService();

	@Override
	public Mono<OidcUser> loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
		return this.delegate.loadUser(userRequest)
				.map(user -> {
					final OAuth2AccessToken accessToken = userRequest.getAccessToken();
					return new ShopUserImpl(user, accessToken);
				});
	}

}
