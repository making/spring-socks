package lol.maki.socks.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.authorizeRequests(authorizeRequests -> authorizeRequests
						.requestMatchers(EndpointRequest.to("info", "health", "prometheus")).permitAll()
						.mvcMatchers(HttpMethod.GET, "orders").access("hasRole('TRUSTED_CLIENT') and hasAuthority('SCOPE_order:read')")
						.mvcMatchers(HttpMethod.GET, "orders/**").hasAnyAuthority("SCOPE_order:read")
						.mvcMatchers(HttpMethod.POST, "orders/**").hasAnyAuthority("SCOPE_order:write")
						.anyRequest().authenticated())
				.oauth2ResourceServer(r -> r.jwt(jwt -> {
					final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
					jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new JwtGrantedAuthoritiesConverter());
					jwt.jwtAuthenticationConverter(jwtAuthenticationConverter);
				})).csrf(csrf -> csrf.disable())
				.cors();
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
