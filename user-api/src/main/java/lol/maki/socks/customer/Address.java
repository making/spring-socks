package lol.maki.socks.customer;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import org.immutables.value.Value.Immutable;

@Immutable
public abstract class Address implements Serializable {
	private static final long serialVersionUID = 1L;

	public abstract UUID addressId();

	public abstract String number();

	public abstract String street();

	public abstract String city();

	public abstract String postcode();

	public abstract String country();

	public boolean isSame(Address address) {
		return Objects.equals(this.number(), address.number()) &&
				Objects.equals(this.street(), address.street()) &&
				Objects.equals(this.city(), address.city()) &&
				Objects.equals(this.postcode(), address.postcode()) &&
				Objects.equals(this.country(), address.country());
	}
}
