package lol.maki.socks.oauth;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lol.maki.socks.config.SockProps;
import lol.maki.socks.customer.Customer;
import lol.maki.socks.security.CustomerUserDetails;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class JwtClamsEnhancer implements TokenEnhancer {
	private final ClientDetailsService clientDetailsService;

	private final SockProps sockProps;

	public JwtClamsEnhancer(ClientDetailsService clientDetailsService, SockProps sockProps) {
		this.clientDetailsService = clientDetailsService;
		this.sockProps = sockProps;
	}

	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
		final String issuer = Optional.ofNullable(this.sockProps.getIssuerUrl())
				.orElseGet(() -> ServletUriComponentsBuilder.fromCurrentRequest().build().toString());
		final Map<String, Object> additionalInformation = new LinkedHashMap<>();
		final Instant expiration = accessToken.getExpiration().toInstant();

		final Authentication client = SecurityContextHolder.getContext().getAuthentication();
		final String clientId = client.getName();
		final ClientDetails clientDetails = this.clientDetailsService.loadClientByClientId(clientId);

		additionalInformation.put(JwtClaimNames.ISS, issuer);
		additionalInformation.put(JwtClaimNames.EXP, expiration.getEpochSecond());
		additionalInformation.put(JwtClaimNames.IAT, expiration.minusSeconds(clientDetails.getAccessTokenValiditySeconds()).getEpochSecond());
		additionalInformation.put(JwtClaimNames.AUD, List.of(clientId));
		final String nonce = authentication.getOAuth2Request().getRequestParameters().get((OidcParameterNames.NONCE));
		if (nonce != null) {
			additionalInformation.put(OidcParameterNames.NONCE, nonce);
		}
		if (authentication.getPrincipal() instanceof CustomerUserDetails) {
			final CustomerUserDetails userDetails = (CustomerUserDetails) authentication
					.getPrincipal();
			final Customer account = userDetails.getCustomer();
			additionalInformation.put(JwtClaimNames.SUB, account.customerId().toString());
			additionalInformation.put("given_name", account.firstName());
			additionalInformation.put("family_name", account.lastName());
			additionalInformation.put("name", account.username());
			additionalInformation.put("email", account.email().address());
			additionalInformation.put("email_verified", true);
			// FIXME https://www.iana.org/assignments/jwt/jwt.xhtml
		}
		((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInformation);
		return accessToken;
	}
}