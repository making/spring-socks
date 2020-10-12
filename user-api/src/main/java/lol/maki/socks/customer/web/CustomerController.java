package lol.maki.socks.customer.web;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import lol.maki.socks.customer.Address;
import lol.maki.socks.customer.Card;
import lol.maki.socks.customer.Customer;
import lol.maki.socks.customer.CustomerMapper;
import lol.maki.socks.customer.CustomerService;
import lol.maki.socks.user.spec.CustomerAddressCreateRequest;
import lol.maki.socks.user.spec.CustomerAddressResponse;
import lol.maki.socks.user.spec.CustomerCardCreateRequest;
import lol.maki.socks.user.spec.CustomerCardResponse;
import lol.maki.socks.user.spec.CustomerResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

@RestController
@CrossOrigin
public class CustomerController {
	private final CustomerService customerService;

	private final CustomerMapper customerMapper;

	private final IdGenerator idGenerator;

	public CustomerController(CustomerService customerService, CustomerMapper customerMapper, IdGenerator idGenerator) {
		this.customerService = customerService;
		this.customerMapper = customerMapper;
		this.idGenerator = idGenerator;
	}

	void verifyCustomerId(Jwt jwt, UUID customerId) {
		if (!Objects.equals(UUID.fromString(jwt.getSubject()), customerId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The requested information is not allowed to access.");
		}
	}

	@Deprecated
	@GetMapping(path = "/customers/{customerId}/addresses")
	public ResponseEntity<List<CustomerAddressResponse>> getAddressesByCustomerId(@AuthenticationPrincipal Jwt jwt, @PathVariable("customerId") UUID customerId) {
		this.verifyCustomerId(jwt, customerId);
		final Optional<Customer> customer = this.customerMapper.findByCustomerId(customerId);
		return ResponseEntity.of(customer.map(CustomerHelper::toResponse).map(CustomerResponse::getAddresses));
	}

	@Deprecated
	@GetMapping(path = "/customers/{customerId}/cards")
	public ResponseEntity<List<CustomerCardResponse>> getCardsByCustomerId(@AuthenticationPrincipal Jwt jwt, @PathVariable("customerId") UUID customerId) {
		this.verifyCustomerId(jwt, customerId);
		final Optional<Customer> customer = this.customerMapper.findByCustomerId(customerId);
		return ResponseEntity.of(customer.map(CustomerHelper::toResponse).map(CustomerResponse::getCards));
	}

	@Deprecated
	@GetMapping(path = "/customers/{customerId}")
	public ResponseEntity<CustomerResponse> getCustomerByCustomerId(@AuthenticationPrincipal Jwt jwt, @PathVariable("customerId") UUID customerId) {
		this.verifyCustomerId(jwt, customerId);
		final Optional<Customer> customer = this.customerMapper.findByCustomerId(customerId);
		return ResponseEntity.of(customer.map(CustomerHelper::toResponse));
	}

	@Deprecated
	@GetMapping(path = "/addresses")
	public ResponseEntity<CustomerAddressResponse> getAddresses(@AuthenticationPrincipal Jwt jwt) {
		final UUID customerId = UUID.fromString(jwt.getSubject());
		final Optional<Customer> customer = this.customerMapper.findByCustomerId(customerId);
		return ResponseEntity.of(customer.map(CustomerHelper::toResponse)
				.map(CustomerResponse::getAddresses)
				.filter(a -> !a.isEmpty())
				.map(a -> a.get(0)));
	}

	@GetMapping(path = "/addresses/{addressId}")
	public ResponseEntity<CustomerAddressResponse> getAddressesById(@AuthenticationPrincipal Jwt jwt, @PathVariable("addressId") UUID addressId) {
		final UUID customerId = UUID.fromString(jwt.getSubject());
		final Optional<Customer> customer = this.customerMapper.findByCustomerId(customerId);
		return ResponseEntity.of(customer.flatMap(c -> c.addresses().stream()
				.filter(a -> Objects.equals(a.addressId(), addressId))
				.map(CustomerHelper::toCustomerAddressResponse)
				.findAny()));
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

	@GetMapping(path = "/cards/{cardId}")
	public ResponseEntity<CustomerCardResponse> getCardsById(@AuthenticationPrincipal Jwt jwt, @PathVariable("cardId") UUID cardId) {
		final UUID customerId = UUID.fromString(jwt.getSubject());
		final Optional<Customer> customer = this.customerMapper.findByCustomerId(customerId);
		return ResponseEntity.of(customer.flatMap(c -> c.cards().stream()
				.filter(d -> Objects.equals(d.cardId(), cardId))
				.map(CustomerHelper::toCustomerCardResponse)
				.findAny()));
	}

	@Deprecated
	@GetMapping(path = "/cards")
	public ResponseEntity<CustomerCardResponse> getCustomerCardsById(@AuthenticationPrincipal Jwt jwt) {
		final UUID customerId = UUID.fromString(jwt.getSubject());
		final Optional<Customer> customer = this.customerMapper.findByCustomerId(customerId);
		return ResponseEntity.of(customer.map(CustomerHelper::toResponse)
				.map(CustomerResponse::getCards)
				.filter(c -> !c.isEmpty())
				.map(c -> c.get(0)));
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
