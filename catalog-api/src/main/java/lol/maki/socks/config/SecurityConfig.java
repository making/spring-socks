package lol.maki.socks.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.authorizeRequests(authorizeRequests -> authorizeRequests
						.requestMatchers(EndpointRequest.to("info", "health", "prometheus")).permitAll()
						.mvcMatchers("/images/**").permitAll()
						.mvcMatchers(HttpMethod.GET, "/**").hasAuthority("SCOPE_catalog:read")
						.anyRequest().authenticated()
				)
				.oauth2ResourceServer(r -> r.jwt())
				.csrf(csrf -> csrf.disable())
				.cors();
	}

}
