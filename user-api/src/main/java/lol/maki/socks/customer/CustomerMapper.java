package lol.maki.socks.customer;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import static java.util.stream.Collectors.toUnmodifiableList;

@Repository
public class CustomerMapper {
	private final JdbcTemplate jdbcTemplate;

	private static final String BASE_SQL = "SELECT c.customer_id, c.first_name, c.last_name, c.username, c.email, c.allow_duplicate_email, p.password, a.address_id, a.number, a.street, a.city, a.country, a.postcode, d.card_id, d.long_num, d.expires,  d.ccv FROM customer AS c INNER JOIN customer_password AS p ON c.customer_id = p.customer_id LEFT OUTER JOIN customer_address AS a ON c.customer_id = a.customer_id LEFT OUTER JOIN customer_card AS d ON c.customer_id = d.customer_id";

	private final ResultSetExtractor<Customer> customerResultSetExtractor = rs -> {
		Customer customer = null;
		while (rs.next()) {
			if (customer == null) {
				customer = new Customer(
						UUID.fromString(rs.getString("customer_id")),
						rs.getString("username"),
						rs.getString("password"),
						rs.getString("first_name"),
						rs.getString("last_name"),
						new Email(rs.getString("email")),
						rs.getBoolean("allow_duplicate_email"));
			}
			final String addressId = rs.getString("address_id");
			if (addressId != null) {
				customer.addresses()
						.add(new Address(
								UUID.fromString(addressId),
								rs.getString("number"),
								rs.getString("street"),
								rs.getString("city"),
								rs.getString("postcode"),
								rs.getString("country")));
			}
			final String cardId = rs.getString("card_id");
			if (cardId != null) {
				customer.cards()
						.add(new Card(
								UUID.fromString(cardId),
								rs.getString("long_num"),
								rs.getDate("expires").toLocalDate(),
								rs.getString("ccv")));
			}
		}
		return customer;
	};

	public CustomerMapper(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional
	public int[] upsert(Customer customer) {
		int upsertCustomer = this.jdbcTemplate.update("INSERT INTO customer(customer_id, first_name, last_name, username, email, allow_duplicate_email) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE first_name = ?, last_name = ?, username = ?, email = ?",
				customer.customerId().toString(),
				customer.firstName(),
				customer.lastName(),
				customer.username(),
				customer.email().address(),
				customer.allowDuplicateEmail(),
				customer.firstName(),
				customer.lastName(),
				customer.username(),
				customer.email().address());
		final int updatePassword = this.jdbcTemplate.update("INSERT INTO customer_password(customer_id, password) VALUES (?, ?) ON DUPLICATE KEY UPDATE password = ?",
				customer.customerId().toString(),
				customer.password(),
				customer.password());
		int batchUpdateAddress = 0;
		if (!CollectionUtils.isEmpty(customer.addresses())) {
			final int[] updated = this.jdbcTemplate.batchUpdate("INSERT INTO customer_address(address_id, customer_id, number, street, city, postcode, country) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE customer_id = ?, number = ?, street = ?, city = ?, postcode = ?, country = ?",
					customer.addresses().stream()
							.map(a -> new Object[] { a.addressId().toString(), customer.customerId().toString(), a.number(), a.street(), a.city(), a.postcode(), a.country(), customer.customerId().toString(), a.number(), a.street(), a.city(), a.postcode(), a.country() })
							.collect(toUnmodifiableList()));
			batchUpdateAddress = Arrays.stream(updated).sum();
		}
		int batchUpdateCard = 0;
		if (!CollectionUtils.isEmpty(customer.cards())) {
			final int[] updated = this.jdbcTemplate.batchUpdate("INSERT INTO customer_card(card_id, customer_id, long_num, expires, ccv) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE customer_id = ?, long_num = ?, expires = ?, ccv = ?",
					customer.cards().stream()
							.map(d -> new Object[] { d.cardId().toString(), customer.customerId().toString(), d.longNum(), d.expires(), d.ccv(), customer.customerId().toString(), d.longNum(), d.expires(), d.ccv() })
							.collect(toUnmodifiableList()));
			batchUpdateCard = Arrays.stream(updated).sum();
		}
		return new int[] { upsertCustomer, updatePassword, batchUpdateAddress, batchUpdateCard };
	}

	public Optional<Customer> findByUsername(String username) {
		try {
			final Customer customer = this.jdbcTemplate.query(BASE_SQL + " WHERE c.username = ? ORDER BY a.created_at DESC, d.created_at DESC", this.customerResultSetExtractor, username);
			return Optional.ofNullable(customer);
		}
		catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public Optional<Customer> findByEmail(Email email) {
		try {
			final Customer customer = this.jdbcTemplate.query(BASE_SQL + " WHERE c.email = ? AND c.allow_duplicate_email = FALSE ORDER BY a.created_at DESC, d.created_at DESC", this.customerResultSetExtractor, email.address());
			return Optional.ofNullable(customer);
		}
		catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public Optional<Customer> findByCustomerId(UUID customerId) {
		try {
			final Customer customer = this.jdbcTemplate.query(BASE_SQL + " WHERE c.customer_id = ? ORDER BY a.created_at DESC, d.created_at DESC", this.customerResultSetExtractor, customerId.toString());
			return Optional.ofNullable(customer);
		}
		catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}
}
