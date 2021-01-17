package lol.maki.socks.customer.web;

import java.util.UUID;

import lol.maki.socks.customer.Address;
import lol.maki.socks.customer.Card;
import lol.maki.socks.customer.Customer;
import lol.maki.socks.customer.Email;
import lol.maki.socks.user.spec.CustomerAddressCreateRequest;
import lol.maki.socks.user.spec.CustomerAddressResponse;
import lol.maki.socks.user.spec.CustomerCardCreateRequest;
import lol.maki.socks.user.spec.CustomerCardResponse;
import lol.maki.socks.user.spec.CustomerCreateRequest;
import lol.maki.socks.user.spec.CustomerResponse;

import static java.util.stream.Collectors.toUnmodifiableList;

public class CustomerHelper {
	static CustomerResponse toResponse(Customer customer) {
		return new CustomerResponse()
				.firstName(customer.firstName())
				.lastName(customer.lastName())
				.email(customer.email().address())
				.username(customer.username())
				.addresses(customer.addresses() != null ? customer.addresses().stream()
						.map(CustomerHelper::toCustomerAddressResponse)
						.collect(toUnmodifiableList()) : null)
				.cards(customer.cards() != null ? customer.cards().stream()
						.map(CustomerHelper::toCustomerCardResponse)
						.collect(toUnmodifiableList()) : null);
	}

	static CustomerAddressResponse toCustomerAddressResponse(Address address) {
		return new CustomerAddressResponse()
				.addressId(address.addressId())
				.number(address.number())
				.street(address.street())
				.city(address.city())
				.country(address.country())
				.postcode(address.postcode());

	}

	static CustomerCardResponse toCustomerCardResponse(Card card) {
		return new CustomerCardResponse()
				.cardId(card.cardId())
				.longNum(card.longNum())
				.expires(card.expires())
				.ccv(card.ccv());
	}


	static Address fromCustomerAddressCreateRequest(UUID addressId, CustomerAddressCreateRequest req) {
		return new Address(
				addressId,
				req.getNumber(),
				req.getStreet(),
				req.getCity(),
				req.getPostcode(),
				req.getCountry());
	}

	static Customer fromCustomerCreateRequest(UUID customerId, String encodedPassword, CustomerCreateRequest request) {
		return new Customer(
				customerId,
				request.getUsername(),
				encodedPassword,
				request.getFirstName(),
				request.getLastName(),
				new Email(request.getEmail()),
				request.getAllowDuplicateEmail());
	}

	static Card fromCustomerCardCreateRequest(UUID cardId, CustomerCardCreateRequest req) {
		return new Card(
				cardId,
				req.getLongNum(),
				req.getExpires(),
				req.getCcv());
	}
}
