package lol.maki.socks.config.discovery;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "simple.dns.enabled", havingValue = "true", matchIfMissing = true)
public class SimpleDnsDiscoveryClient implements ReactiveDiscoveryClient {
	private final Logger log = LoggerFactory.getLogger(SimpleDnsDiscoveryClient.class);

	private static final Pattern IPV4_PATTERN =
			Pattern.compile(
					"^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

	private static final Pattern HOSTNAME_PATTERN = Pattern.compile("^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}");

	private final SimpleDnsDiscoveryProps props;

	public SimpleDnsDiscoveryClient(SimpleDnsDiscoveryProps props) {
		this.props = props;
	}

	@Override
	public String description() {
		return this.getClass().getName();
	}

	@Override
	public Flux<ServiceInstance> getInstances(String serviceId) {
		if (serviceId == null) {
			log.warn("'serviceId' is null.");
			return Flux.empty();
		}
		if (!this.props.isResolveHostname() || "localhost".equals(serviceId) || isIPv4Address(serviceId)) {
			// pass through the service instance
			return Flux.just(this.createServiceInstance(0L, serviceId, serviceId));
		}
		if (!isHostname(serviceId)) {
			return Flux.empty();
		}
		// Resolve serviceId as hostname
		return Flux
				.defer(() -> {
					log.info("Resolving {}", serviceId);
					try {
						final InetAddress[] addresses = Inet4Address.getAllByName(serviceId);
						return Flux.fromStream(Arrays.stream(addresses));
					}
					catch (UnknownHostException e) {
						log.warn("Failed to resolve " + serviceId, e);
						return Flux.empty();
					}
				})
				.subscribeOn(Schedulers.boundedElastic())
				.index()
				.map(tpl -> {
					final ServiceInstance serviceInstance = this.createServiceInstance(tpl.getT1(), serviceId, tpl.getT2().getHostAddress());
					serviceInstance.getMetadata().put("preservedHostname", serviceId);
					return serviceInstance;
				});
	}

	ServiceInstance createServiceInstance(long index, String serviceId, String address) {
		final String instanceId = serviceId + "-" + index;
		final int port = this.props.resolvePort(serviceId);
		return new DefaultServiceInstance(instanceId, serviceId, address, port, this.props.isUseSecuredServiceInstances());
	}

	@Override
	public Flux<String> getServices() {
		log.info("getServices");
		return Flux.empty();
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	static boolean isIPv4Address(String input) {
		return IPV4_PATTERN.matcher(input).matches();
	}

	static boolean isHostname(String input) {
		return HOSTNAME_PATTERN.matcher(input).matches();
	}
}
