package lol.maki.socks.order;

import org.immutables.value.Value.Immutable;

@Immutable
public abstract class Address {
	public abstract String number();

	public abstract String street();

	public abstract String city();

	public abstract String postcode();

	public abstract String country();
}
