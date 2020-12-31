package lol.maki.socks.config.discovery;

import java.util.Collections;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "simple.dns")
@ConstructorBinding
public class SimpleDnsDiscoveryProps {
	private final boolean resolveHostname;

	private final boolean useSecuredServiceInstances;

	private final Map<String, Map<PortType, Integer>> ports;

	private final Map<PortType, Integer> defaultPorts;

	public SimpleDnsDiscoveryProps(@DefaultValue("true") boolean resolveHostname, @DefaultValue("false") boolean useSecuredServiceInstances, Map<String, Map<PortType, Integer>> ports, Map<PortType, Integer> defaultPorts) {
		this.resolveHostname = resolveHostname;
		this.useSecuredServiceInstances = useSecuredServiceInstances;
		this.ports = ports == null ? Collections.emptyMap() : ports;
		this.defaultPorts = defaultPorts == null ? Collections.emptyMap() : defaultPorts;
	}

	public boolean isResolveHostname() {
		return resolveHostname;
	}

	public boolean isUseSecuredServiceInstances() {
		return useSecuredServiceInstances;
	}

	public int resolvePort(String serviceId) {
		final PortType portType = this.useSecuredServiceInstances ? PortType.HTTPS : PortType.HTTP;
		final int defaultPort = this.defaultPorts.getOrDefault(portType, portType.defaultPort);
		if (!this.ports.containsKey(serviceId)) {
			return this.defaultPorts.getOrDefault(portType, defaultPort);
		}
		return this.ports.get(serviceId).getOrDefault(portType, defaultPort);
	}

	enum PortType {
		HTTP(80), HTTPS(443);

		final int defaultPort;

		PortType(int defaultPort) {
			this.defaultPort = defaultPort;
		}
	}
}
