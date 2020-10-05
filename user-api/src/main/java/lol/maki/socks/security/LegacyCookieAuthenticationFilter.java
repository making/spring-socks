package lol.maki.socks.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

public class LegacyCookieAuthenticationFilter extends OncePerRequestFilter {
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		final Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			Arrays.stream(cookies)
					.filter(c -> Objects.equals(c.getName(), "logged_in"))
					.findFirst()
					.map(Cookie::getValue)
					.ifPresent(loggedIn -> {
						final PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken(loggedIn, "", AuthorityUtils.createAuthorityList("SCOPE_openid", "SCOPE_customer:read", "SCOPE_customer:write"));
						final SecurityContext context = SecurityContextHolder.createEmptyContext();
						context.setAuthentication(token);
						SecurityContextHolder.setContext(context);
					});
		}
		filterChain.doFilter(request, response);
	}
}
