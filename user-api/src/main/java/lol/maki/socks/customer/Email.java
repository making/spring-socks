package lol.maki.socks.customer;

import java.io.Serializable;

import org.immutables.value.Value.Immutable;

@Immutable
public abstract class Email implements Serializable {
	private static final long serialVersionUID = 1L;

	public abstract String address();
}
