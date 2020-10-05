package lol.maki.socks.customer.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
	@GetMapping(path = "loginForm")
	public String login() {
		return "login";
	}
}
