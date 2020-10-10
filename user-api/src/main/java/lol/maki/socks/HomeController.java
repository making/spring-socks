package lol.maki.socks;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@ConfigurationProperties(prefix = "home")
public class HomeController {
	private String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@GetMapping(path = "/")
	public String home() {
		return "redirect:" + this.url;
	}
}
