package lol.maki.socks.config;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ClientResponse.Headers;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public class LoggingExchangeFilterFunction implements ExchangeFilterFunction {

	private final Logger log = LoggerFactory.getLogger(LoggingExchangeFilterFunction.class);

	private final boolean includeBody;

	public LoggingExchangeFilterFunction(boolean includeBody) {
		this.includeBody = includeBody;
	}

	@Override
	public Mono<ClientResponse> filter(ClientRequest clientRequest, ExchangeFunction exchangeFunction) {
		final AtomicLong begin = new AtomicLong();
		final ClientRequest request;
		if (log.isDebugEnabled()) {
			final StringBuilder builder = new StringBuilder(clientRequest.method().toString())
					.append(" ")
					.append(clientRequest.url())
					.append(System.lineSeparator())
					.append(clientRequest.headers().entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue().stream()
							.collect(Collectors.joining(",")))
							.collect(Collectors.joining(System.lineSeparator())));
			log.debug("--> {}", builder);
			final BodyInserter<?, ? super ClientHttpRequest> bodyInserter = clientRequest.body();
			if (this.includeBody) {
				request = ClientRequest.from(clientRequest)
						.body((outputMessage, context) -> bodyInserter.insert(new LoggingClientHttpRequest(outputMessage), context))
						.build();
			}
			else {
				request = clientRequest;
			}
			begin.set(System.currentTimeMillis());
		}
		else {
			request = clientRequest;
		}
		return exchangeFunction.exchange(request)
				.doOnNext(clientResponse -> {
					if (log.isDebugEnabled()) {
						final long elapsed = System.currentTimeMillis() - begin.get();
						final StringBuilder builder = new StringBuilder(clientResponse.statusCode().toString())
								.append(" ")
								.append(clientRequest.url())
								.append(" (")
								.append(elapsed)
								.append("ms)")
								.append(System.lineSeparator())
								.append(clientResponse.headers().asHttpHeaders().entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue().stream()
										.collect(Collectors.joining(",")))
										.collect(Collectors.joining(System.lineSeparator())));
						log.debug("<-- {}", builder);
					}
				})
				.doOnCancel(() -> {
					final long elapsed = System.currentTimeMillis() - begin.get();
					log.debug("<-- CANCELED {} ({}ms)", clientRequest.url(), elapsed);
				})
				.doOnError(e -> {
					if (log.isDebugEnabled()) {
						final long elapsed = System.currentTimeMillis() - begin.get();
						final Object status = (e instanceof WebClientResponseException) ? ((WebClientResponseException) e).getStatusCode() : "000";
						log.debug("<-- {} {} ({}ms)", status, clientRequest.url(), elapsed);
					}
				})
				.flatMap(clientResponse -> {
					final Headers headers = clientResponse.headers();
					if (!log.isDebugEnabled() || !this.includeBody || headers.contentLength().isPresent() && headers.contentLength().getAsLong() == 0L) {
						return Mono.just(clientResponse);
					}
					return clientResponse.bodyToMono(String.class)
							.doOnNext(r -> {
								log.debug("");
								final MediaType contentType = headers.contentType().orElse(MediaType.ALL);
								final String type = contentType.getType();
								final String subtype = contentType.getSubtype();
								if (subtype.endsWith("json") || subtype.endsWith("xml") || type.equals("text")) {
									log.debug("{}", r);
								}
								else {
									log.debug("<omitted {}>", contentType);
								}
							})
							.map(body -> clientResponse.mutate().body(body).build());
				});
	}

	class LoggingClientHttpRequest implements ClientHttpRequest {
		private final ClientHttpRequest delegate;

		LoggingClientHttpRequest(ClientHttpRequest delegate) {
			this.delegate = delegate;
		}

		@Override
		public HttpHeaders getHeaders() {
			return this.delegate.getHeaders();
		}

		@Override
		public DataBufferFactory bufferFactory() {
			return this.delegate.bufferFactory();
		}

		@Override
		public void beforeCommit(Supplier<? extends Mono<Void>> action) {
			this.delegate.beforeCommit(action);
		}

		@Override
		public boolean isCommitted() {
			return this.delegate.isCommitted();
		}

		@Override
		public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
			return log.isDebugEnabled() ? this.delegate.writeWith(DataBufferUtils.join(body)
					.doOnNext(data -> {
						log.debug("");
						log.debug("{}", data.toString(StandardCharsets.UTF_8));
					})) : this.delegate.writeWith(body);
		}

		@Override
		public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
			return log.isDebugEnabled() ? this.delegate.writeAndFlushWith(Flux.from(body)
					.map(b -> DataBufferUtils.join(b).doOnNext(data -> {
						log.debug("");
						log.debug("{}", data.toString(StandardCharsets.UTF_8));
					}))) : this.delegate.writeAndFlushWith(body);
		}

		@Override
		public Mono<Void> setComplete() {
			return this.delegate.setComplete();
		}

		@Override
		public HttpMethod getMethod() {
			return this.delegate.getMethod();
		}

		@Override
		public URI getURI() {
			return this.delegate.getURI();
		}

		@Override
		public MultiValueMap<String, HttpCookie> getCookies() {
			return this.delegate.getCookies();
		}

		@Override
		public <T> T getNativeRequest() {
			return this.delegate.getNativeRequest();
		}
	}
}
