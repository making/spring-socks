package lol.maki.socks.config;

import java.net.URI;

import lol.maki.socks.cart.CartClient;
import lol.maki.socks.cart.web.MergeCartServerAuthenticationSuccessHandler;
import lol.maki.socks.security.RedirectToServerRedirectStrategy;
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect;

import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.reactive.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.DelegatingServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
public class SecurityConfig {
	private final URI authorizationServerLogoutUrl;

	private final CartClient cartClient;

	public SecurityConfig(OAuth2ClientProperties clientProperties, @Lazy CartClient cartClient) {
		this.authorizationServerLogoutUrl = clientProperties.getProvider().values().stream().findFirst()
				.map(provider -> {
					if (provider.getAuthorizationUri() != null) {
						return provider.getAuthorizationUri();
					}
					else {
						return provider.getIssuerUri();
					}
				})
				.map(UriComponentsBuilder::fromHttpUrl)
				.map(builder -> builder.replacePath("logout").build().toUri())
				.orElseThrow();
		this.cartClient = cartClient;
	}

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http
				.authorizeExchange(exchanges -> exchanges
						.matchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
						.matchers(EndpointRequest.to("health", "info", "prometheus")).permitAll()
						.anyExchange().permitAll()
				)
				.oauth2Login(oauth2 -> {
					final MergeCartServerAuthenticationSuccessHandler mergeCartServerAuthenticationSuccessHandler = new MergeCartServerAuthenticationSuccessHandler(this.cartClient);
					final RedirectServerAuthenticationSuccessHandler redirectServerAuthenticationSuccessHandler = new RedirectServerAuthenticationSuccessHandler();
					redirectServerAuthenticationSuccessHandler.setRedirectStrategy(new RedirectToServerRedirectStrategy());
					oauth2.authenticationSuccessHandler(new DelegatingServerAuthenticationSuccessHandler(
							mergeCartServerAuthenticationSuccessHandler,
							redirectServerAuthenticationSuccessHandler));
				})
				.logout(logout -> {
					final RedirectServerLogoutSuccessHandler redirectServerLogoutSuccessHandler = new RedirectServerLogoutSuccessHandler();
					redirectServerLogoutSuccessHandler.setLogoutSuccessUrl(this.authorizationServerLogoutUrl);
					logout.logoutSuccessHandler(redirectServerLogoutSuccessHandler);
				})
				.csrf(csrf -> csrf.disable() /* TODO */)
				.build();
	}

	@Bean
	public ReactiveOAuth2AuthorizedClientManager reactiveOAuth2AuthorizedClientManager(ReactiveClientRegistrationRepository clientRegistrationRepository,
			ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
		final ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
				.clientCredentials()
				.build();
		final DefaultReactiveOAuth2AuthorizedClientManager authorizedClientManager = new DefaultReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);
		authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
		return authorizedClientManager;
	}

	@Bean
	public SpringSecurityDialect securityDialect() {
		return new SpringSecurityDialect();
	}
}
