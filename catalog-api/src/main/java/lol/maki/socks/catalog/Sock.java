package lol.maki.socks.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.immutables.value.Value.Immutable;

@Immutable
public abstract class Sock {
	public abstract UUID sockId();

	public abstract String name();

	public abstract String description();

	public abstract BigDecimal price();

	public abstract int count();

	public abstract List<String> imageUrl();

	public abstract List<Tag> tags();
}
