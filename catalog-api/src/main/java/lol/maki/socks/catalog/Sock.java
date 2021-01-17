package lol.maki.socks.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class Sock {
	private final UUID sockId;

	private final String name;

	private final String description;

	private final BigDecimal price;

	private final int count;

	private final List<String> imageUrl;

	private final List<Tag> tags;

	public Sock(UUID sockId, String name, String description, BigDecimal price, int count, List<String> imageUrl, List<Tag> tags) {
		this.sockId = sockId;
		this.name = name;
		this.description = description;
		this.price = price;
		this.count = count;
		this.imageUrl = imageUrl;
		this.tags = tags;
	}

	public UUID sockId() {
		return sockId;
	}

	public String name() {
		return name;
	}

	public String description() {
		return description;
	}

	public BigDecimal price() {
		return price;
	}

	public int count() {
		return count;
	}

	public List<String> imageUrl() {
		return imageUrl;
	}

	public List<Tag> tags() {
		return tags;
	}
}
