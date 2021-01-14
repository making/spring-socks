package lol.maki.socks.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.ForwardedHeaderFilter;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Configuration
@Order(-1)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.addFilterBefore(new ForwardedHeaderFilter(), LogoutFilter.class)
				.formLogin(formLogin -> formLogin
						.loginPage("/loginForm")
						.loginProcessingUrl("/doLogin")
						.usernameParameter("username")
						.passwordParameter("password")
						.permitAll())
				.logout(logout -> logout
						.logoutRequestMatcher(new AntPathRequestMatcher("/logout") /* supports GET /logout */)
						.permitAll())
				.requestMatchers(requestMatchers -> requestMatchers
						.mvcMatchers("/", "/loginForm", "/doLogin", "/logout", "/oauth/authorize", "token_keys", "/.well-known/*", "/oauth/token/.well-known/*", "/me", "/customers/**", "/addresses/**", "/cards/**")
						.requestMatchers(EndpointRequest.toAnyEndpoint()))
				.authorizeRequests(authorize -> authorize
						.mvcMatchers("/").permitAll()
						.mvcMatchers("/oauth/authorize", "/token_keys", "/.well-known/*", "/oauth/token/.well-known/*").permitAll()
						.mvcMatchers("/me").hasAnyAuthority("SCOPE_openid")
						.mvcMatchers(GET, "/customers/**").access("hasRole('TRUSTED_CLIENT') and hasAuthority('SCOPE_customer:read')")
						.mvcMatchers(POST, "/customers").access("hasRole('TRUSTED_CLIENT') and hasAuthority('SCOPE_customer:write')")
						.mvcMatchers(POST, "/addresses", "/cards").hasAnyAuthority("SCOPE_customer:write")
						.requestMatchers(EndpointRequest.to("info", "health", "prometheus")).permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(r -> r.jwt(jwt -> {
					final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
					jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new JwtGrantedAuthoritiesConverter());
					jwt.jwtAuthenticationConverter(jwtAuthenticationConverter);
				}))
				.csrf(csrf -> csrf.disable());
	}

	static class JwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

		@Override
		public Collection<GrantedAuthority> convert(Jwt jwt) {
			final List<GrantedAuthority> authorities = new ArrayList<>();
			for (String scope : jwt.getClaimAsStringList("scope")) {
				authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
			}
			for (String role : jwt.getClaimAsStringList("authorities")) {
				authorities.add(new SimpleGrantedAuthority(role));
			}
			return authorities;
		}
	}
}