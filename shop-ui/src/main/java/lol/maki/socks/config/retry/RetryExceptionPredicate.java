package lol.maki.socks.config.retry;

import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.oauth2.core.OAuth2AuthorizationException;

public class RetryExceptionPredicate implements Predicate<Throwable> {
	private final Logger log = LoggerFactory.getLogger(RetryExceptionPredicate.class);

	@Override
	public boolean test(Throwable throwable) {
		boolean retry = false;
		if (throwable instanceof OAuth2AuthorizationException) {
			retry = true;
		}
		if (retry) {
			log.info("Retry => " + throwable);
		}
		return retry;
	}
}
