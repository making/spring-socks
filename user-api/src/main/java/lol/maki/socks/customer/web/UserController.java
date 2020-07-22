package lol.maki.socks.customer.web;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import lol.maki.socks.LoggedInUser;
import lol.maki.socks.customer.Customer;
import lol.maki.socks.customer.CustomerMapper;
import lol.maki.socks.user.spec.CustomerCreateRequest;
import lol.maki.socks.user.spec.CustomerResponse;
import lol.maki.socks.user.spec.LoginApi;
import lol.maki.socks.user.spec.MeApi;
import lol.maki.socks.user.spec.RegisterApi;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Base64Utils;
import org.springframework.util.IdGenerator;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin
public class UserController implements LoginApi, RegisterApi, MeApi {
	private final CustomerMapper customerMapper;

	private final PasswordEncoder passwordEncoder;

	private final IdGenerator idGenerator;

	private final LoggedInUser loggedInUser;

	public UserController(CustomerMapper customerMapper, PasswordEncoder passwordEncoder, IdGenerator idGenerator, LoggedInUser loggedInUser) {
		this.customerMapper = customerMapper;
		this.passwordEncoder = passwordEncoder;
		this.idGenerator = idGenerator;
		this.loggedInUser = loggedInUser;
	}

	// Legacy Endpoint
	@Override
	public ResponseEntity<Void> login(String authorization) {
		final String[] userInfo = new String(Base64Utils.decodeFromString(authorization.replace("Basic ", ""))).split(":");
		final String username = userInfo[0];
		final String password = userInfo[1];
		final Customer customer = this.customerMapper.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
		if (this.passwordEncoder.matches(password, customer.password())) {
			final ResponseCookie cookie = ResponseCookie
					.from("logged_in", customer.customerId().toString())
					.httpOnly(false)
					.maxAge(Duration.ofDays(1))
					.build();
			return ResponseEntity.ok()
					.header(HttpHeaders.SET_COOKIE, cookie.toString())
					.build();
		}
		else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@Override
	public ResponseEntity<CustomerResponse> register(CustomerCreateRequest request) {
		final UUID customerId = this.idGenerator.generateId();
		final String encodedPassword = this.passwordEncoder.encode(request.getPassword());
		final Customer customer = CustomerHelper.fromCustomerCreateRequest(customerId, encodedPassword, request);
		this.customerMapper.upsert(customer);
		return ResponseEntity.ok(CustomerHelper.toResponse(customer));
	}

	@Override
	public ResponseEntity<CustomerResponse> getMe() {
		final UUID customerId = this.loggedInUser.customerId();
		final Optional<Customer> customer = this.customerMapper.findByCustomerId(customerId);
		return ResponseEntity.of(customer.map(CustomerHelper::toResponse));
	}

}
