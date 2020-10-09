package lol.maki.socks.customer.web;

import java.util.UUID;

import lol.maki.socks.customer.Address;
import lol.maki.socks.customer.Card;
import lol.maki.socks.customer.Customer;
import lol.maki.socks.customer.ImmutableAddress;
import lol.maki.socks.customer.ImmutableCard;
import lol.maki.socks.customer.ImmutableCustomer;
import lol.maki.socks.customer.ImmutableEmail;
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
		return ImmutableAddress.builder()
				.addressId(addressId)
				.number(req.getNumber())
				.street(req.getStreet())
				.city(req.getCity())
				.country(req.getCountry())
				.postcode(req.getPostcode())
				.build();
	}

	static Customer fromCustomerCreateRequest(UUID customerId, String encodedPassword, CustomerCreateRequest request) {
		return ImmutableCustomer.builder()
				.customerId(customerId)
				.firstName(request.getFirstName())
				.lastName(request.getLastName())
				.username(request.getUsername())
				.password(encodedPassword)
				.email(ImmutableEmail.builder().address(request.getEmail()).build())
				.allowDuplicateEmail(request.getAllowDuplicateEmail())
				.build();
	}

	static Card fromCustomerCardCreateRequest(UUID cardId, CustomerCardCreateRequest req) {
		return ImmutableCard.builder()
				.cardId(cardId)
				.longNum(req.getLongNum())
				.expires(req.getExpires())
				.ccv(req.getCcv())
				.build();
	}
}
