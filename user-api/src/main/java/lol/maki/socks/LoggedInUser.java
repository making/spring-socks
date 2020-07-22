package lol.maki.socks;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class LoggedInUser {
	private static final Logger log = LoggerFactory.getLogger(LoggedInUser.class);

	public UUID customerId() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof JwtAuthenticationToken) {
			final Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
			return UUID.fromString(jwt.getSubject());
		}
		if (authentication instanceof PreAuthenticatedAuthenticationToken) {
			final String customerId = authentication.getPrincipal().toString();
			log.warn("** DANGER: Using legacy cookie-based access. **");
			return UUID.fromString(customerId);
		}
		return null;
	}
}
