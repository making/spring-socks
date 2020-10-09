package lol.maki.socks.security;

import java.util.Collection;

import lol.maki.socks.customer.Customer;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomerUserDetails implements UserDetails {
	private final Customer customer;

	public CustomerUserDetails(Customer customer) {
		this.customer = customer;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		if (customer.allowDuplicateEmail()) {
			return AuthorityUtils.createAuthorityList("ROLE_TEMPORAL");
		}
		return AuthorityUtils.createAuthorityList("ROLE_USER");
	}


	public Customer getCustomer() {
		return this.customer;
	}

	@Override
	public String getPassword() {
		return this.customer.password();
	}

	@Override
	public String getUsername() {
		return this.customer.username();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
