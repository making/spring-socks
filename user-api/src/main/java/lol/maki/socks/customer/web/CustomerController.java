package lol.maki.socks.customer.web;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import lol.maki.socks.LoggedInUser;
import lol.maki.socks.customer.Address;
import lol.maki.socks.customer.Card;
import lol.maki.socks.customer.Customer;
import lol.maki.socks.customer.CustomerMapper;
import lol.maki.socks.customer.ImmutableCustomer;
import lol.maki.socks.user.spec.AddressesApi;
import lol.maki.socks.user.spec.CardsApi;
import lol.maki.socks.user.spec.CustomerAddressCreateRequest;
import lol.maki.socks.user.spec.CustomerAddressResponse;
import lol.maki.socks.user.spec.CustomerCardCreateRequest;
import lol.maki.socks.user.spec.CustomerCardResponse;
import lol.maki.socks.user.spec.CustomerResponse;
import lol.maki.socks.user.spec.CustomersApi;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.IdGenerator;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin
public class CustomerController implements CustomersApi, AddressesApi, CardsApi {
	private final CustomerMapper customerMapper;

	private final IdGenerator idGenerator;

	private final LoggedInUser loggedInUser;

	public CustomerController(CustomerMapper customerMapper, IdGenerator idGenerator, LoggedInUser loggedInUser) {
		this.customerMapper = customerMapper;
		this.idGenerator = idGenerator;
		this.loggedInUser = loggedInUser;
	}

	void verifyCustomerId(UUID customerId) {
		if (!Objects.equals(this.loggedInUser.customerId(), customerId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The requested information is not allowed to access.");
		}
	}

	@Override
	public ResponseEntity<List<CustomerAddressResponse>> getAddressesByCustomerId(UUID customerId) {
		this.verifyCustomerId(customerId);
		final Optional<Customer> customer = this.customerMapper.findByCustomerId(customerId);
		return ResponseEntity.of(customer.map(CustomerHelper::toResponse).map(CustomerResponse::getAddresses));
	}

	@Override
	public ResponseEntity<List<CustomerCardResponse>> getCardsByCustomerId(UUID customerId) {
		this.verifyCustomerId(customerId);
		final Optional<Customer> customer = this.customerMapper.findByCustomerId(customerId);
		return ResponseEntity.of(customer.map(CustomerHelper::toResponse).map(CustomerResponse::getCards));
	}

	@Override
	public ResponseEntity<CustomerResponse> getCustomerByCustomerId(UUID customerId) {
		this.verifyCustomerId(customerId);
		final Optional<Customer> customer = this.customerMapper.findByCustomerId(customerId);
		return ResponseEntity.of(customer.map(CustomerHelper::toResponse));
	}

	@Override
	public ResponseEntity<List<CustomerAddressResponse>> getAddresses() {
		final UUID customerId = this.loggedInUser.customerId();
		final Optional<Customer> customer = this.customerMapper.findByCustomerId(customerId);
		return ResponseEntity.of(customer.map(CustomerHelper::toResponse).map(CustomerResponse::getAddresses));
	}

	@Override
	public ResponseEntity<CustomerAddressResponse> postAddresses(CustomerAddressCreateRequest request) {
		final UUID customerId = this.loggedInUser.customerId();
		final Optional<Customer> customer = this.customerMapper.findByCustomerId(customerId);
		final UUID addressId = this.idGenerator.generateId();
		final Address newAddress = CustomerHelper.fromCustomerAddressCreateRequest(addressId, request);
		return ResponseEntity.of(customer.map(c -> ImmutableCustomer.builder()
				.from(c)
				.addAddresses(newAddress)
				.build())
				.map(c -> {
					this.customerMapper.upsert(c);
					return CustomerHelper.toCustomerAddressResponse(newAddress);
				}));
	}

	@Override
	public ResponseEntity<List<CustomerCardResponse>> getCustomerCardsById() {
		final UUID customerId = this.loggedInUser.customerId();
		final Optional<Customer> customer = this.customerMapper.findByCustomerId(customerId);
		return ResponseEntity.of(customer.map(CustomerHelper::toResponse).map(CustomerResponse::getCards));
	}

	@Override
	public ResponseEntity<CustomerCardResponse> postCards(CustomerCardCreateRequest request) {
		final UUID customerId = this.loggedInUser.customerId();
		final Optional<Customer> customer = this.customerMapper.findByCustomerId(customerId);
		final UUID cardId = this.idGenerator.generateId();
		final Card newCard = CustomerHelper.fromCustomerCardCreateRequest(cardId, request);
		return ResponseEntity.of(customer.map(c -> ImmutableCustomer.builder()
				.from(c)
				.addCards(newCard)
				.build())
				.map(c -> {
					this.customerMapper.upsert(c);
					return CustomerHelper.toCustomerCardResponse(newCard);
				}));
	}

}
