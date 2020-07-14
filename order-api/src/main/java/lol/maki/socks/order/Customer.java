package lol.maki.socks.order;

import org.immutables.value.Value.Immutable;

@Immutable
public abstract class Customer {
	public abstract String id();

	public abstract String firstName();

	public abstract String lastName();

	public abstract String username();
}
