package lol.maki.socks.catalog.web;

import java.util.List;
import java.util.UUID;

import lol.maki.socks.catalog.Sock;
import lol.maki.socks.catalog.SockMapper;
import lol.maki.socks.catalog.Tag;
import lol.maki.socks.catalog.spec.CountResponse;
import lol.maki.socks.catalog.spec.SockResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@CrossOrigin
public class CatalogController {
	private final SockMapper sockMapper;

	public CatalogController(SockMapper sockMapper) {
		this.sockMapper = sockMapper;
	}

	@GetMapping(path = "/catalogue/{id}")
	public ResponseEntity<SockResponse> getSock(@PathVariable("id") UUID id) {
		return ResponseEntity.of(this.sockMapper.findOne(id)
				.map(this::toResponse));
	}

	@GetMapping(path = "/catalogue/size")
	public ResponseEntity<CountResponse> getSockCount(@RequestParam(value = "tags", required = false) List<String> tags) {
		final long count = this.sockMapper.countByTag(tags == null ? null : tags.stream().map(Tag::valueOf).collect(toUnmodifiableList()));
		return ResponseEntity.ok(new CountResponse().size(count));
	}

	@GetMapping(path = "/catalogue")
	public ResponseEntity<List<SockResponse>> getSocks(@RequestParam(value = "order", required = false, defaultValue = "price") String order,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
			@RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
			@RequestParam(value = "tags", required = false) List<String> tags) {
		final List<Sock> socks = this.sockMapper.findSocks(tags == null ? null : tags.stream().map(Tag::valueOf).collect(toUnmodifiableList()), order, page, size);
		return ResponseEntity.ok(socks.stream().map(this::toResponse).collect(toUnmodifiableList()));
	}

	SockResponse toResponse(Sock sock) {
		return new SockResponse()
				.id(sock.sockId())
				.name(sock.name())
				.description(sock.description())
				.price(sock.price())
				.count(sock.count())
				.imageUrl(sock.imageUrl())
				.tag(sock.tags().stream().map(Tag::name).collect(toUnmodifiableList()));
	}
}
