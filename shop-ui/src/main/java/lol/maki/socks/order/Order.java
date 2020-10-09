package lol.maki.socks.order;

public class Order {
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

	private String password;

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
				", password='" + password + '\'' +
				'}';
	}
}
