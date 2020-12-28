package lol.maki.socks.config.circuitbreaker;

import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecordFailurePredicate implements Predicate<Throwable> {
	private final Logger log = LoggerFactory.getLogger(RecordFailurePredicate.class);

	@Override
	public boolean test(Throwable throwable) {
		log.info("Record " + throwable);
		return true;
	}
}
