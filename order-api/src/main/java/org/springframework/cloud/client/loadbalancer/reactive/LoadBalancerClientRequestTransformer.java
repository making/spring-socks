package org.springframework.cloud.client.loadbalancer.reactive;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.core.annotation.Order;
import org.springframework.web.reactive.function.client.ClientRequest;

/**
 * https://github.com/spring-cloud/spring-cloud-commons/issues/873
 *
 * Allows applications to transform the load-balanced {@link ClientRequest} given the chosen
 * {@link ServiceInstance}.
 *
 * @author Toshiaki Maki
 */
@Order(LoadBalancerClientRequestTransformer.DEFAULT_ORDER)
public interface LoadBalancerClientRequestTransformer {
	/**
	 * Order for the load balancer request tranformer.
	 */
	int DEFAULT_ORDER = 0;

	ClientRequest transformRequest(ClientRequest request, ServiceInstance instance);
}
