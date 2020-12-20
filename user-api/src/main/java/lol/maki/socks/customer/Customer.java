package lol.maki.socks.customer;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.immutables.value.Value.Immutable;

import org.springframework.lang.Nullable;

@Immutable
public abstract class Customer implements Serializable {
	private static final long serialVersionUID = 1L;

	public abstract UUID customerId();

	public abstract String username();

	public abstract String password();

	public abstract String firstName();

	public abstract String lastName();

	public abstract Email email();

	public abstract boolean allowDuplicateEmail();

	@Nullable
	public abstract List<Address> addresses();

	@Nullable
	public abstract List<Card> cards();
}
