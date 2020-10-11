package lol.maki.socks.order;

import java.time.LocalDate;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;
import am.ik.yavi.core.ViolationMessage;

public class Order {
	static Validator<Order> validator = ValidatorBuilder.of(Order.class)
			.constraint(Order::getFirstName, "firstName", c -> c.notBlank())
			.constraint(Order::getLastName, "lastName", c -> c.notBlank())
			.constraint(Order::getStreet, "street", c -> c.notBlank())
			.constraint(Order::getNumber, "number", c -> c.notBlank())
			.constraint(Order::getCity, "city", c -> c.notBlank())
			.constraint(Order::getPostcode, "postcode", c -> c.notBlank())
			.constraint(Order::getCountry, "country", c -> c.notBlank())
			.constraint(Order::getEmail, "email", c -> c.notBlank()
					.email())
			.constraint(Order::getLongNum, "longNum", c -> c.notBlank()
					.pattern("[0-9]*").message("\"{0}\" contains only numbers")
					.greaterThanOrEqual(14)
					.lessThanOrEqual(16))
			.constraint(Order::getExpires, "expires", c -> c.notBlank()
					.predicate(expires -> {
						final LocalDate date = Order.parseExpires(expires);
						return date != null && date.isAfter(LocalDate.now());
					}, ViolationMessage.of("expires", "\"{0}\" must be a valid \"MM/YY\".")))
			.constraint(Order::getCcv, "ccv", c -> c.notBlank()
					.pattern("[0-9]*").message("\"{0}\" contains only numbers"))
			.constraintOnCondition((order, group) -> order.isCreateAccount(), b -> b
					.constraint(Order::getUsername, "username", c -> c.notBlank()
							.greaterThanOrEqual(4))
					.constraint(Order::getPassword, "password", c -> c.notBlank()
							.greaterThanOrEqual(6)))
			.build();

	private String firstName;

	private String lastName;

	private String street;

	private String number;

	private String city;

	private String postcode;

	private String country;

	private String email;

	private String longNum;

	private String expires;

	private String ccv;

	private boolean createAccount = false;

	private String username;

	private String password;

	public ConstraintViolations validate() {
		return validator.validate(this);
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLongNum() {
		return longNum;
	}

	public void setLongNum(String longNum) {
		this.longNum = longNum;
	}

	public String getExpires() {
		return expires;
	}

	public void setExpires(String expires) {
		this.expires = expires;
	}

	public void setExpires(LocalDate expires) {
		this.expires = expires.getMonthValue() + "/" + (expires.getYear() - 2000);
	}

	public LocalDate parseExpires() {
		return parseExpires(this.expires);
	}

	static LocalDate parseExpires(String expires) {
		if (expires == null) {
			return null;
		}
		final String[] split = expires.split("\\-|/");
		try {
			final int year = Integer.parseInt(split[1]) + 2000;
			final int month = Integer.parseInt(split[0]);
			return LocalDate.of(year, month, 1)
					.plusMonths(1)
					.minusDays(1);
		}
		catch (RuntimeException e) {
			return null;
		}
	}

	public String getCcv() {
		return ccv;
	}

	public void setCcv(String ccv) {
		this.ccv = ccv;
	}

	public boolean isCreateAccount() {
		return createAccount;
	}

	public void setCreateAccount(boolean createAccount) {
		this.createAccount = createAccount;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	@Override
	public String toString() {
		return "Order{" +
				"firstName='" + firstName + '\'' +
				", lastName='" + lastName + '\'' +
				", street='" + street + '\'' +
				", number='" + number + '\'' +
				", city='" + city + '\'' +
				", postcode='" + postcode + '\'' +
				", country='" + country + '\'' +
				", email='" + email + '\'' +
				", longNum='" + longNum + '\'' +
				", expires='" + expires + '\'' +
				", ccv='" + ccv + '\'' +
				", createAccount=" + createAccount +
				", username='" + username + '\'' +
				", password='" + password + '\'' +
				'}';
	}
}
