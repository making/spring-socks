package lol.maki.socks.login;

import java.time.Duration;
import java.util.Map;

import lol.maki.socks.LoggedInUser;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
	@GetMapping(path = "login")
	public Mono<?> login(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorization, ServerHttpResponse response) {
		System.out.println(new String(Base64Utils.decodeFromString(authorization.replace("Basic ", ""))));
		final ResponseCookie cookie = ResponseCookie
				.from("logged_in", LoggedInUser.customerId())
				.httpOnly(false)
				.maxAge(Duration.ofDays(1))
				.build();
		response.addCookie(cookie);
		return Mono.just("OK");
	}

	@GetMapping(path = "customers/{customerId}")
	public Mono<?> username(@PathVariable("customerId") String customerId) {
		return Mono.just("{\n"
				+ "  \"lastName\": \"Doe\",\n"
				+ "  \"firstName\": \"John\"\n"
				+ "}");
	}
}
