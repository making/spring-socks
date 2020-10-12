package lol.maki.socks.customer.web;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import lol.maki.socks.customer.Customer;
import lol.maki.socks.customer.CustomerMapper;
import lol.maki.socks.customer.CustomerService;
import lol.maki.socks.customer.CustomerService.CustomerDuplicatedException;
import lol.maki.socks.user.spec.CustomerCreateRequest;
import lol.maki.socks.user.spec.CustomerResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.Base64Utils;
import org.springframework.util.IdGenerator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin
public class UserController {
	private final CustomerService customerService;

	private final CustomerMapper customerMapper;

	private final PasswordEncoder passwordEncoder;

	private final IdGenerator idGenerator;

	public UserController(CustomerService customerService, CustomerMapper customerMapper, PasswordEncoder passwordEncoder, IdGenerator idGenerator) {
		this.customerService = customerService;
		this.customerMapper = customerMapper;
		this.passwordEncoder = passwordEncoder;
		this.idGenerator = idGenerator;
	}

	// Legacy Endpoint
	@Deprecated
	@PostMapping(path = "/login")
	public ResponseEntity<Void> login(@RequestHeader(name = "Authorization") String authorization) {
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

	@Deprecated
	@PostMapping(path = "/register")
	public ResponseEntity<CustomerResponse> register(@Validated @RequestBody CustomerCreateRequest request) {
		final UUID customerId = this.idGenerator.generateId();
		final String encodedPassword = request.getPassword() != null ? this.passwordEncoder.encode(request.getPassword()) : null;
		final Customer customer = CustomerHelper.fromCustomerCreateRequest(customerId, encodedPassword, request);
		try {
			this.customerService.createCustomer(customer);
		}
		catch (CustomerDuplicatedException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
		}
		return ResponseEntity.ok(CustomerHelper.toResponse(customer));
	}

	@GetMapping(path = "/me")
	public ResponseEntity<CustomerResponse> getMe(@AuthenticationPrincipal Jwt jwt) {
		final UUID customerId = UUID.fromString(jwt.getSubject());
		final Optional<Customer> customer = this.customerMapper.findByCustomerId(customerId);
		return ResponseEntity.of(customer.map(CustomerHelper::toResponse));
	}

}
