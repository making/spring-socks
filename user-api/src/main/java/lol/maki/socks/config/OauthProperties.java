package lol.maki.socks.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

@ConfigurationProperties(prefix = "oauth")
public class OauthProperties {
	private final List<BaseClientDetails> clients;

	@ConstructorBinding
	public OauthProperties(List<BaseClientDetails> clients) {
		this.clients = clients.stream()
				.peek(client -> client.setAuthorities(AuthorityUtils.createAuthorityList("ROLE_TRUSTED_CLIENT")))
				.collect(Collectors.toList());
	}

	public Map<String, ClientDetails> getClients() {
		return clients.stream()
				.collect(Collectors.toMap(ClientDetails::getClientId, x -> x));
	}
}