package lol.maki.socks.config.discovery;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;

public class SimpleDnsDiscoverySupport {
	private static final Pattern IPV4_PATTERN =
			Pattern.compile(
					"^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

	private static final Pattern HOSTNAME_PATTERN = Pattern.compile("^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}");

	protected final SimpleDnsDiscoveryProps props;

	public SimpleDnsDiscoverySupport(SimpleDnsDiscoveryProps props) {
		this.props = props;
	}

	public String description() {
		return this.getClass().getName();
	}

	List<ServiceInstance> resolveServiceInstances(String serviceId) {
		// Resolve serviceId as hostname
		try {
			final InetAddress[] addresses = Inet4Address.getAllByName(serviceId);
			final ArrayList<ServiceInstance> instances = new ArrayList<>(addresses.length);
			for (int i = 0; i < addresses.length; i++) {
				final ServiceInstance instance = this.createServiceInstance(i, serviceId, addresses[i].getHostAddress());
				instance.getMetadata().put("preservedHostname", serviceId);
				instances.add(instance);
			}
			return instances;
		}
		catch (UnknownHostException e) {
			return Collections.emptyList();
		}
	}

	ServiceInstance createServiceInstance(int index, String serviceId, String address) {
		final String instanceId = serviceId + "-" + index;
		final int port = this.props.resolvePort(serviceId);
		return new DefaultServiceInstance(instanceId, serviceId, address, port, this.props.isUseSecuredServiceInstances());
	}

	boolean isPassThrough(String serviceId) {
		return !this.props.isResolveHostname() || "localhost".equals(serviceId) || isIPv4Address(serviceId);
	}

	static boolean isIPv4Address(String input) {
		return IPV4_PATTERN.matcher(input).matches();
	}

	static boolean isHostname(String input) {
		return HOSTNAME_PATTERN.matcher(input).matches();
	}
}
