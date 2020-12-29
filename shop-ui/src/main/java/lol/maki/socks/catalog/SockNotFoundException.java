package lol.maki.socks.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import lol.maki.socks.catalog.client.SockResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public class SockNotFoundException extends RuntimeException {
	private final UUID id;

	public SockNotFoundException(UUID id) {
		super("The given sock (id=" + id + ") is not found.");
		this.id = id;
	}

	public static void throwIfNotFound(UUID id, Throwable throwable) {
		if (throwable instanceof WebClientResponseException) {
			final WebClientResponseException ex = (WebClientResponseException) throwable;
			final HttpStatus statusCode = ex.getStatusCode();
			if (statusCode == HttpStatus.NOT_FOUND) {
				throw new SockNotFoundException(id);
			}
		}
	}

	public SockResponse notFound() {
		return new SockResponse()
				.name("Not Found")
				.description("Sorry, the requested item is not found.")
				.imageUrl(List.of("/img/internet_404_page_not_found.png", "/img/internet_404_page_not_found_j.png"))
				.price(BigDecimal.ZERO)
				.tag(List.of())
				.id(this.id)
				.count(0);
	}
}
