package lol.maki.socks.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Order(0) // Higher than DefaultErrorWebExceptionHandler
@ControllerAdvice
public class ExceptionHandlerAdvice {
	private final Logger log = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

	@ExceptionHandler(WebClientResponseException.class)
	public ResponseEntity<?> handleWebClientResponseException(WebClientResponseException e) {
		log.warn(e.getMessage());
		return ResponseEntity.status(e.getStatusCode())
				.contentType(MediaType.APPLICATION_JSON)
				.body(e.getResponseBodyAsString());
	}
}
