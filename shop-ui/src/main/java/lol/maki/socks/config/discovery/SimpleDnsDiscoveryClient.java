package lol.maki.socks.config.discovery;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "simple.dns.enabled", havingValue = "true", matchIfMissing = true)
public class SimpleDnsDiscoveryClient extends SimpleDnsDiscoverySupport implements DiscoveryClient {
	private final Logger log = LoggerFactory.getLogger(SimpleDnsDiscoveryClient.class);

	public SimpleDnsDiscoveryClient(SimpleDnsDiscoveryProps props) {
		super(props);
	}

	@Override
	public List<ServiceInstance> getInstances(String serviceId) {
		if (serviceId == null) {
			log.warn("'serviceId' is null.");
			return Collections.emptyList();
		}
		if (super.isPassThrough(serviceId)) {
			// pass through the service instance
			return Collections.singletonList(super.createServiceInstance(0, serviceId, serviceId));
		}
		// Resolve serviceId as hostname
		log.info("Resolving {}", serviceId);
		return super.resolveServiceInstances(serviceId);
	}

	@Override
	public List<String> getServices() {
		log.info("getServices");
		return Collections.emptyList();
	}
}
