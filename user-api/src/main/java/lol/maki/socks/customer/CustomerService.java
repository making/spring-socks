package lol.maki.socks.customer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {
	private final CustomerMapper customerMapper;

	public CustomerService(CustomerMapper customerMapper) {
		this.customerMapper = customerMapper;
	}

	@Transactional
	public int[] createCustomer(Customer customer) throws CustomerDuplicatedException {
		if (this.customerMapper.findByUsername(customer.username()).isPresent()) {
			throw new CustomerDuplicatedException("The requested username is already registered.");
		}
		if (customer.password() != null && this.customerMapper.findByEmail(customer.email()).isPresent()) {
			throw new CustomerDuplicatedException("The requested email is already registered.");
		}
		return this.customerMapper.upsert(customer);
	}

	public static class CustomerDuplicatedException extends RuntimeException {
		public CustomerDuplicatedException(String message) {
			super(message);
		}
	}
}
