package lol.maki.socks.config.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreaker.EventPublisher;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Component
public class LoggingCircuitBreakerRegistryEventConsumer implements RegistryEventConsumer<CircuitBreaker> {
	private final Logger log = LoggerFactory.getLogger(LoggingCircuitBreakerRegistryEventConsumer.class);

	@Override
	public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
		final CircuitBreaker circuitBreaker = entryAddedEvent.getAddedEntry();
		final EventPublisher eventPublisher = circuitBreaker.getEventPublisher();
		eventPublisher.onStateTransition(event -> log.info("{}: {}", event.getCircuitBreakerName(), event.getStateTransition()));
	}

	@Override
	public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) {

	}

	@Override
	public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) {

	}
}
