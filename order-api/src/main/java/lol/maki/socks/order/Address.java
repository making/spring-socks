package lol.maki.socks.order;


public class Address {
	private final String number;

	private final String street;

	private final String city;

	private final String postcode;

	private final String country;

	public Address(String number, String street, String city, String postcode, String country) {
		this.number = number;
		this.street = street;
		this.city = city;
		this.postcode = postcode;
		this.country = country;
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
}
