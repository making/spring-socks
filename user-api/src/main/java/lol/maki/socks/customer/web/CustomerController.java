package lol.maki.socks.customer.web;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import lol.maki.socks.customer.Address;
import lol.maki.socks.customer.Card;
import lol.maki.socks.customer.Customer;
import lol.maki.socks.customer.CustomerMapper;
import lol.maki.socks.customer.CustomerService;
import lol.maki.socks.customer.CustomerService.CustomerDuplicatedException;
import lol.maki.socks.user.spec.CustomerAddressCreateRequest;
import lol.maki.socks.user.spec.CustomerAddressResponse;
import lol.maki.socks.user.spec.CustomerCardCreateRequest;
import lol.maki.socks.user.spec.CustomerCardResponse;
import lol.maki.socks.user.spec.CustomerCreateRequest;
import lol.maki.socks.user.spec.CustomerResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.IdGenerator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@CrossOrigin
public class CustomerController {
	private final CustomerService customerService;

	private final CustomerMapper customerMapper;

	private final PasswordEncoder passwordEncoder;

	private final IdGenerator idGenerator;

	public CustomerController(CustomerService customerService, CustomerMapper customerMapper, PasswordEncoder passwordEncoder, IdGenerator idGenerator) {
		this.customerService = customerService;
		this.customerMapper = customerMapper;
		this.passwordEncoder = passwordEncoder;
		this.idGenerator = idGenerator;
	}

	@GetMapping(path = "/me")
	public ResponseEntity<CustomerResponse> getMe(@AuthenticationPrincipal Jwt jwt) {
		final UUID customerId = UUID.fromString(jwt.getSubject());
		final Optional<Customer> customer = this.customerMapper.findByCustomerId(customerId);
		return ResponseEntity.of(customer.map(CustomerHelper::toResponse));
	}

	@PostMapping(path = "/customers")
	public ResponseEntity<CustomerResponse> register(@Validated @RequestBody CustomerCreateRequest request, UriComponentsBuilder builder) {
		final UUID customerId = this.idGenerator.generateId();
		final String encodedPassword = request.getPassword() != null ? this.passwordEncoder.encode(request.getPassword()) : null;
		final Customer customer = CustomerHelper.fromCustomerCreateRequest(customerId, encodedPassword, request);
		try {
			this.customerService.createCustomer(customer);
		}
		catch (CustomerDuplicatedException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
		}
		final URI uri = builder.replacePath("customers/{customerId}").build(customerId);
		return ResponseEntity.created(uri).body(CustomerHelper.toResponse(customer));
	}

	@GetMapping(path = "/customers/{customerId}")
	public ResponseEntity<CustomerResponse> getCustomerByCustomerId(@AuthenticationPrincipal Jwt jwt, @PathVariable("customerId") UUID customerId) {
		final Optional<Customer> customer = this.customerMapper.findByCustomerId(customerId);
		return ResponseEntity.of(customer.map(CustomerHelper::toResponse));
	}

	@PostMapping(path = "/addresses")
	public ResponseEntity<CustomerAddressResponse> postAddresses(@AuthenticationPrincipal Jwt jwt, @Validated @RequestBody CustomerAddressCreateRequest request) {
		final UUID customerId = UUID.fromString(jwt.getSubject());
		final Optional<Customer> customer = this.customerMapper.findByCustomerId(customerId);
		final UUID addressId = this.idGenerator.generateId();
		final Address newAddress = CustomerHelper.fromCustomerAddressCreateRequest(addressId, request);
		return ResponseEntity.of(customer.map(c -> {
			final Address address = this.customerService.addAddress(c, newAddress);
			return CustomerHelper.toCustomerAddressResponse(address);
		}));
	}

	@PostMapping(path = "/cards")
	public ResponseEntity<CustomerCardResponse> postCards(@AuthenticationPrincipal Jwt jwt, @Validated @RequestBody CustomerCardCreateRequest request) {
		final UUID customerId = UUID.fromString(jwt.getSubject());
		final Optional<Customer> customer = this.customerMapper.findByCustomerId(customerId);
		final UUID cardId = this.idGenerator.generateId();
		final Card newCard = CustomerHelper.fromCustomerCardCreateRequest(cardId, request);
		return ResponseEntity.of(customer.map(c -> {
			final Card card = this.customerService.addCard(c, newCard);
			return CustomerHelper.toCustomerCardResponse(card);
		}));
	}
}
