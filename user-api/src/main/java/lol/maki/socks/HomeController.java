package lol.maki.socks;

import lol.maki.socks.config.SockProps;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
	private final SockProps props;

	public HomeController(SockProps props) {
		this.props = props;
	}

	@GetMapping(path = "/")
	public String home() {
		return "redirect:" + this.props.getLoginUrl();
	}
}
