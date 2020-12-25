package lol.maki.socks.config;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public enum LoggingExchangeFilterFunction implements ExchangeFilterFunction {
	SINGLETON {
		@Override
		public Mono<ClientResponse> filter(ClientRequest clientRequest, ExchangeFunction exchangeFunction) {
			final AtomicLong begin = new AtomicLong();
			if (log().isDebugEnabled()) {
				log().info("--> {} {}", clientRequest.method(), clientRequest.url());
				clientRequest.headers().forEach((k, v) -> {
					log().info("{}: {}", k, String.join(",", v));
				});
				log().info("--> END {}", clientRequest.method());
				begin.set(System.currentTimeMillis());
			}
			return exchangeFunction.exchange(clientRequest)
					.doOnNext(clientResponse -> {
						if (log().isDebugEnabled()) {
							final long elapsed = System.currentTimeMillis() - begin.get();
							log().info("<-- {} {} ({}ms)", clientResponse.statusCode(), clientRequest.url(), elapsed);
							clientResponse.headers().asHttpHeaders().forEach((k, v) -> {
								log().info("{}: {}", k, String.join(",", v));
							});
							log().info("<-- END HTTP");
						}
					})
					.doOnError(e -> {
						if (log().isDebugEnabled()) {
							final long elapsed = System.currentTimeMillis() - begin.get();
							final Object status = (e instanceof WebClientResponseException) ? ((WebClientResponseException) e).getStatusCode() : "000";
							log().info("<-- {} {} ({}ms)", status, clientRequest.url(), elapsed);
						}
						log().info("<-- FAILED HTTP");
					});
		}
	};

	private final Logger log = LoggerFactory.getLogger(LoggingExchangeFilterFunction.class);

	final Logger log() {
		return log;
	}
}
