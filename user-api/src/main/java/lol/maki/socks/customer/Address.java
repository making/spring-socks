package lol.maki.socks.customer;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Address implements Serializable {
	private static final long serialVersionUID = 1L;

	private final UUID addressId;

	private final String number;

	private final String street;

	private final String city;

	private final String postcode;

	private final String country;

	public Address(UUID addressId, String number, String street, String city, String postcode, String country) {
		this.addressId = addressId;
		this.number = number;
		this.street = street;
		this.city = city;
		this.postcode = postcode;
		this.country = country;
	}

	public UUID addressId() {
		return addressId;
	}

	public String number() {
		return number;
	}

	public String street() {
		return street;
	}

	public String city() {
		return city;
	}

	public String postcode() {
		return postcode;
	}

	public String country() {
		return country;
	}

	public boolean isSame(Address address) {
		return Objects.equals(this.number(), address.number()) &&
				Objects.equals(this.street(), address.street()) &&
				Objects.equals(this.city(), address.city()) &&
				Objects.equals(this.postcode(), address.postcode()) &&
				Objects.equals(this.country(), address.country());
	}
}
