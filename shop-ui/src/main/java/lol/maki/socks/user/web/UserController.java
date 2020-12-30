package lol.maki.socks.user.web;

import java.util.Collections;

import lol.maki.socks.security.ShopUser;
import lol.maki.socks.user.UserClient;
import reactor.core.publisher.Mono;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
	private final UserClient userClient;

	public UserController(UserClient userClient) {
		this.userClient = userClient;
	}

	@GetMapping(path = "me")
	public Mono<?> me(@AuthenticationPrincipal ShopUser user) {
		if (user == null) {
			return Mono.just(Collections.emptyMap());
		}
		return this.userClient.getMe();
	}
}
