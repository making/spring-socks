package lol.maki.socks.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WarmupRunner implements CommandLineRunner {
	private final WebClient webClient;

	public WarmupRunner(WebClient.Builder builder, @Value("${server.port:8080}") int serverPort) {
		this.webClient = builder
				.baseUrl("http://localhost:" + serverPort)
				.filter(new LoggingExchangeFilterFunction(false))
				.build();
	}

	@Override
	public void run(String... args) throws Exception {
		this.webClient.get()
				.uri("/")
				.retrieve()
				.toBodilessEntity()
				.block();
	}
}
