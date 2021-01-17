package lol.maki.socks.order;

public class Customer {
	private final String id;

	private final String firstName;

	private final String lastName;

	private final String username;

	public Customer(String id, String firstName, String lastName, String username) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.username = username;
	}

	public String id() {
		return id;
	}

	public String firstName() {
		return firstName;
	}

	public String lastName() {
		return lastName;
	}

	public String username() {
		return username;
	}
}
