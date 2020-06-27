package lol.maki.socks.catalog;

import java.util.concurrent.ConcurrentHashMap;

public interface Tag {
	String name();

	static Tag valueOf(String value) {
		return cache.computeIfAbsent(value, k -> () -> value);
	}

	ConcurrentHashMap<String, Tag> cache = new ConcurrentHashMap<>();
}
