package lol.maki.socks.security;

import java.util.Optional;

import lol.maki.socks.customer.Customer;
import lol.maki.socks.customer.CustomerMapper;
import lol.maki.socks.customer.Email;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomerUserDetailsService implements UserDetailsService {
	private final CustomerMapper customerMapper;

	public CustomerUserDetailsService(CustomerMapper customerMapper) {
		this.customerMapper = customerMapper;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		final Optional<Customer> customer = username.contains("@") ?
				this.customerMapper.findByEmail(new Email(username)) :
				this.customerMapper.findByUsername(username);
		return customer.map(CustomerUserDetails::new)
				.orElseThrow(() -> new UnsupportedOperationException("The given username is not found."));
	}
}
