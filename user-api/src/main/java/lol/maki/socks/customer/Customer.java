package lol.maki.socks.customer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Customer implements Serializable {
	private static final long serialVersionUID = 1L;

	private final UUID customerId;

	private final String username;

	private final String password;

	private final String firstName;

	private final String lastName;

	private final Email email;

	private final boolean allowDuplicateEmail;

	private final List<Address> addresses = new ArrayList<>();

	private final List<Card> cards = new ArrayList<>();

	public Customer(UUID customerId, String username, String password, String firstName, String lastName, Email email, boolean allowDuplicateEmail) {
		this.customerId = customerId;
		this.username = username;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.allowDuplicateEmail = allowDuplicateEmail;
	}

	public static long serialVersionUID() {
		return serialVersionUID;
	}

	public UUID customerId() {
		return customerId;
	}

	public String username() {
		return username;
	}

	public String password() {
		return password;
	}

	public String firstName() {
		return firstName;
	}

	public String lastName() {
		return lastName;
	}

	public Email email() {
		return email;
	}

	public boolean allowDuplicateEmail() {
		return allowDuplicateEmail;
	}

	public List<Address> addresses() {
		return addresses;
	}

	public List<Card> cards() {
		return cards;
	}
}
