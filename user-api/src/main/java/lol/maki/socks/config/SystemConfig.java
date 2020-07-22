package lol.maki.socks.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.IdGenerator;
import org.springframework.util.JdkIdGenerator;

@Configuration
public class SystemConfig {

	@Bean
	public Clock systemClock() {
		return Clock.systemDefaultZone();
	}

	@Bean
	public IdGenerator idGenerator() {
		return new JdkIdGenerator();
	}
}
