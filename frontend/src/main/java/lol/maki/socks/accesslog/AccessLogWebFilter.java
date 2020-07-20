package lol.maki.socks.accesslog;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import brave.Span;
import brave.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import static org.springframework.http.HttpHeaders.REFERER;

@Component
public class AccessLogWebFilter implements WebFilter {
	private final Tracer tracer;

	private final Logger log = LoggerFactory.getLogger("RTR");

	public AccessLogWebFilter(Tracer tracer) {
		this.tracer = tracer;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		final long begin = System.nanoTime();
		return chain.filter(exchange)
				.doFinally(__ -> {
					final long elapsed = (System.nanoTime() - begin) / 1_000_000;
					final ServerHttpRequest request = exchange.getRequest();
					final ServerHttpResponse response = exchange.getResponse();
					final OffsetDateTime now = OffsetDateTime.now();
					final HttpMethod method = request.getMethod();
					final RequestPath path = request.getPath();
					final HttpStatus code = response.getStatusCode();
					final int statusCode = code == null ? 0 : code.value();
					final HttpHeaders headers = request.getHeaders();
					final String host = headers.getHost().getHostString();
					final String address = request.getRemoteAddress().getHostString();
					final String userAgent = headers.getFirst(HttpHeaders.USER_AGENT);
					final String referer = headers.getFirst(REFERER);
					if (!userAgent.startsWith("kube-probe")) {
						final AccessLog accessLog = new AccessLogBuilder()
								.setDate(now.toString())
								.setMethod(Objects.toString(method, ""))
								.setPath(path.value()).setStatus(statusCode)
								.setHost(host).setAddress(address).setElapsed(elapsed)
								.setUserAgent(userAgent).setReferer(referer)
								.build();
						final List<String> xForwardedFors = headers.get("X-Forwarded-For");
						final String xForwardedFor = xForwardedFors == null ? null : String.join(", ", xForwardedFors);
						final String xForwardedProto = headers.getFirst("X-Forwarded-Proto");
						final Span span = this.tracer.currentSpan();
						span.tag("host", host);
						if (referer != null) {
							span.tag("referer", referer);
						}
						span.tag("user-agent", userAgent);
						log.info("{}", accessLog.goRouterCompliant(xForwardedFor, xForwardedProto, span));
					}
				});
	}
}
