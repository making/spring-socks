package lol.maki.socks.config;

import lol.maki.socks.security.LegacyCookieAuthenticationFilter;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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
				.formLogin(formLogin -> formLogin
						.loginPage("/loginForm")
						.loginProcessingUrl("/doLogin")
						.usernameParameter("username")
						.passwordParameter("password")
						.permitAll())
				.addFilterAfter(new LegacyCookieAuthenticationFilter(), LogoutFilter.class)
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
						.mvcMatchers(GET, "/addresses/**", "/cards/**", "/customers/**").hasAnyAuthority("SCOPE_customer:read")
						.mvcMatchers(POST, "/addresses", "/cards").hasAnyAuthority("SCOPE_customer:write")
						.requestMatchers(EndpointRequest.to("info", "health", "prometheus")).permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(r -> r.jwt())
				.csrf(csrf -> csrf.disable());
	}
}