package lol.maki.socks.catalog.web;

import java.util.List;
import java.util.UUID;

import lol.maki.socks.catalog.Sock;
import lol.maki.socks.catalog.SockMapper;
import lol.maki.socks.catalog.Tag;
import lol.maki.socks.catalog.spec.CatalogueApi;
import lol.maki.socks.catalog.spec.CountResponse;
import lol.maki.socks.catalog.spec.SockResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@CrossOrigin
public class CatalogController implements CatalogueApi {
	private final SockMapper sockMapper;

	public CatalogController(SockMapper sockMapper) {
		this.sockMapper = sockMapper;
	}

	@Override
	public ResponseEntity<SockResponse> getSock(UUID id) {
		return ResponseEntity.of(this.sockMapper.findOne(id)
				.map(this::toResponse));
	}

	@Override
	public ResponseEntity<CountResponse> getSockCount(List<String> tags) {
		final long count = this.sockMapper.countByTag(tags == null ? null : tags.stream().map(Tag::valueOf).collect(toUnmodifiableList()));
		return ResponseEntity.ok(new CountResponse().size(count));
	}

	@Override
	public ResponseEntity<List<SockResponse>> getSocks(String order, Integer page, Integer size, List<String> tags) {
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
