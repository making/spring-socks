package lol.maki.socks.customer;

import java.util.List;
import java.util.UUID;

import org.immutables.value.Value.Immutable;

@Immutable
public abstract class Customer {
	public abstract UUID customerId();

	public abstract String username();

	public abstract String password();

	public abstract String firstName();

	public abstract String lastName();

	public abstract Email email();

	public abstract List<Address> addresses();

	public abstract List<Card> cards();
}
