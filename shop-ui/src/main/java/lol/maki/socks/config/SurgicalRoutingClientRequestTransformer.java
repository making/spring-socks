package lol.maki.socks.config;

import java.util.Map;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerClientRequestTransformer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;

@Component
public class SurgicalRoutingClientRequestTransformer implements LoadBalancerClientRequestTransformer {
	public static final String APPLICATION_ID = "application_id";

	public static final String INSTANCE_INDEX = "instance_index";

	@Override
	public ClientRequest transformRequest(ClientRequest request, ServiceInstance instance) {
		final Map<String, String> metadata = instance.getMetadata();
		if (metadata.containsKey(APPLICATION_ID) && metadata.containsKey(INSTANCE_INDEX)) {
			return ClientRequest.from(request)
					.header("X-Cf-App-Instance", String.format("%s:%s", metadata.get(APPLICATION_ID), metadata.get(INSTANCE_INDEX)))
					.build();
		}
		return request;
	}
}
