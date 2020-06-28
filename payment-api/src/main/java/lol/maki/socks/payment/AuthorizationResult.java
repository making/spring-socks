package lol.maki.socks.payment;

import org.immutables.value.Value.Immutable;

@Immutable
public abstract class AuthorizationResult {
	public abstract boolean authorized();

	public abstract boolean valid();

	public abstract String message();
}
