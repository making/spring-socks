package lol.maki.socks.oauth;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lol.maki.socks.config.JwtProperties;
import lol.maki.socks.config.SockProps;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;


@RestController
public class WellKnownEndpoint {
	private final KeyPair keyPair;

	private final SockProps sockProps;

	public WellKnownEndpoint(JwtProperties jwtProps, SockProps sockProps) {
		this.keyPair = jwtProps.getKeyPair();
		this.sockProps = sockProps;
	}

	/**
	 * https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfig
	 */
	@GetMapping(path = { ".well-known/openid-configuration", "oauth/token/.well-known/openid-configuration" })
	public Map<String, Object> openIdConfiguration(UriComponentsBuilder builder) {
		final String issuerUrl = Optional.ofNullable(this.sockProps.getIssuerUrl())
				.orElseGet(() -> builder.replacePath("oauth/token").build().toString());
		final UriComponentsBuilder issuerBuilder = UriComponentsBuilder.fromHttpUrl(issuerUrl);
		return Map.of("issuer", issuerUrl,
				"authorization_endpoint", issuerBuilder.replacePath("oauth/authorize").build().toString(),
				"token_endpoint", issuerBuilder.replacePath("oauth/token").build().toString(),
				"jwks_uri", issuerBuilder.replacePath("token_keys").build().toString(),
				"subject_types_supported", List.of("public"));
	}

	/**
	 * https://docs.spring.io/spring-security-oauth2-boot/docs/2.3.x-SNAPSHOT/reference/html5/#oauth2-boot-authorization-server-spring-security-oauth2-resource-server-jwk-set-uri
	 */
	@GetMapping(path = "token_keys")
	public Map<String, Object> tokenKeys() {
		final RSAPublicKey publicKey = (RSAPublicKey) this.keyPair.getPublic();
		final RSAKey key = new RSAKey.Builder(publicKey).build();
		return new JWKSet(key).toJSONObject();
	}
}
