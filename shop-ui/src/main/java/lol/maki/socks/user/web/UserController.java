package lol.maki.socks.user.web;

import java.util.Collections;
import java.util.Map;

import lol.maki.socks.security.ShopUser;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
	@GetMapping(path = "me")
	public Map<String, Object> me(@AuthenticationPrincipal ShopUser user) {
		if (user == null) {
			return Collections.emptyMap();
		}
		return user.getClaims();
	}
}
