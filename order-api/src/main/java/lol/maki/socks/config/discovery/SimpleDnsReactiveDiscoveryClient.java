package lol.maki.socks.config.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "simple.dns.enabled", havingValue = "true", matchIfMissing = true)
public class SimpleDnsReactiveDiscoveryClient extends SimpleDnsDiscoverySupport implements ReactiveDiscoveryClient {
	private final Logger log = LoggerFactory.getLogger(SimpleDnsReactiveDiscoveryClient.class);

	public SimpleDnsReactiveDiscoveryClient(SimpleDnsDiscoveryProps props) {
		super(props);
	}

	@Override
	public Flux<ServiceInstance> getInstances(String serviceId) {
		if (serviceId == null) {
			log.warn("'serviceId' is null.");
			return Flux.empty();
		}
		if (super.isPassThrough(serviceId)) {
			// pass through the service instance
			return Flux.just(super.createServiceInstance(0, serviceId, serviceId));
		}
		// Resolve serviceId as hostname
		return Flux
				.defer(() -> {
					log.info("Resolving {}", serviceId);
					return Flux.fromIterable(super.resolveServiceInstances(serviceId));
				})
				.subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public Flux<String> getServices() {
		log.info("getServices");
		return Flux.empty();
	}
}
