package lol.maki.socks.customer;

import java.io.Serializable;

public class Email implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String address;

	public Email(String address) {
		this.address = address;
	}

	public String address() {
		return address;
	}
}
