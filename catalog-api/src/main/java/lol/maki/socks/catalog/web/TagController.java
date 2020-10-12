package lol.maki.socks.catalog.web;

import java.util.List;

import lol.maki.socks.catalog.Tag;
import lol.maki.socks.catalog.TagMapper;
import lol.maki.socks.catalog.spec.TagsResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@CrossOrigin
public class TagController {
	private final TagMapper tagMapper;

	public TagController(TagMapper tagMapper) {
		this.tagMapper = tagMapper;
	}

	@GetMapping(path = "tags")
	public ResponseEntity<TagsResponse> getTags() {
		final List<String> tags = this.tagMapper.findAll().stream()
				.map(Tag::name)
				.collect(toUnmodifiableList());
		return ResponseEntity.ok(new TagsResponse().tags(tags));
	}
}
