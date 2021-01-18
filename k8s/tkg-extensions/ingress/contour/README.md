# Ingress with Contour

## Introduction

Contour is a Kubernetes ingress controller using Lyft's Envoy proxy.

## Deploying Contour

### Prerequisites

* YTT installed (<https://github.com/k14s/ytt/releases>).
* Workload cluster deployed.

### Assumptions

* Using self signed certificates.

### Workload cluster

1. Install cert-manager on the workload cluster if not already installed.

    ```sh
    kubectl apply -f cert-manager/
    ```

2. Create config.yaml file (Supported configurations key/value pairs are below)

   ```yaml
   #@data/values
   ---
   <key1>:<value1>
   <key2>:<value2>
   ```

3. Deploy Contour and Envoy

    ```sh
    ytt --ignore-unknown-comments -f common/ -f ingress/contour/ -f <config.yaml> -v infrastructure_provider="<infra_provider>" | kubectl apply -f-
    ```

## Configurations

The default configuration values are in ingress/contour/values.yaml

| Parameter                                        | Description                                                                                             | Type             | Default                                                                         |
|--------------------------------------------------|---------------------------------------------------------------------------------------------------------|------------------|---------------------------------------------------------------------------------|
| infrastructure_provider                          | Infrastructure Provider. Supported Values: vsphere, aws, azure                                          | string           | Mandatory parameter                                                             |
| contour.namespace                                | Namespace where contour will be deployed                                                                | string           | tanzu-system-ingress                                                            |
| contour.config.requestTimeout                    | Client request timeout to be passed to Envoy                                                            | time.Duration    | 0s                                                                              |
| contour.config.tls.minimumProtocolVersion        | Minimum TLS version that Contour will negotiate                                                         | string           | 1.1                                                                             |
| contour.config.tls.fallbackCertificate.name      | Name of secret containing fallback certificate for requests that dont match SNI defined for a vhost     | string           | Null                                                                            |
| contour.config.tls.fallbackCertificate.namespace | Namespace of secret containing fallback certificate                                                     | string           | Null                                                                            |
| contour.config.leaderelection.configmapName      | Name of configmap to be used for contour leaderelection                                                 | string           | leader-elect                                                                    |
| contour.config.leaderelection.configmapNamespace | Namespace of contour leaderelection configmap                                                           | string           | tanzu-system-ingress                                                            |
| contour.config.disablePermitInsecure             | Disables ingressroute permitInsecure field                                                              | boolean          | false                                                                           |
| contour.config.accesslogFormat                   | Access log format                                                                                       | string           | envoy                                                                           |
| contour.config.jsonFields                        | Fields that will be logged                                                                              | array of strings | <https://godoc.org/github.com/projectcontour/contour/internal/envoy#JSONFields> |
| contour.config.useProxyProtocol                  | <https://projectcontour.io/guides/proxy-proto/>                                                         | boolean          | false                                                                           |
| contour.config.defaultHTTPVersions               | HTTP versions that Contour should program Envoy to serve                                                | array of strings | "HTTP/1.1 HTTP2"                                                                |
| contour.config.timeouts.requestTimeout           | The timeout for an entire request                                                                       | time.Duration    | Null (timeout is disabled)                                                      |
| contour.config.timeouts.connectionIdleTimeout    | The time to wait before terminating an idle connection                                                  | time.Duration    | 60s                                                                             |
| contour.config.timeouts.streamIdleTimeout        | The time to wait before terminating an request or stream with no activity                               | time.Duration    | 5m                                                                              |
| contour.config.timeouts.maxConnectionDuration    | The time to wait before terminating an connection irrespective of activity or not                       | time.Duration    | Null (timeout is disabled)                                                      |
| contour.config.timeouts.ConnectionShutdownGracePeriod | The time to wait between sending an initial and final GOAWAY                                       | time.Duration    | 5s                                                                              |
| contour.config.debug                             | Turn on contour debugging                                                                               | boolean          | false                                                                           |
| contour.config.ingressStatusAddress              | The address to set on status of every Ingress resource                                                  | string           | Null                                                                            |
| contour.certificate.duration                     | Duration for contour certificate                                                                        | time.Duration    | 8760h                                                                           |
| contour.certificate.renewBefore                  | Duration before contour certificate should be renewed                                                   | time.Duration    | 360h                                                                            |
| contour.deployment.replicas                      | No of contour replicas                                                                                  | integer          | 2                                                                               |
| contour.image.repository                         | Repository containing contour image                                                                     | string           | registry.tkg.vmware.run                                                    |
| contour.image.name                               | Name of contour image                                                                                   | string           | contour                                                                         |
| contour.image.tag                                | Contour image tag                                                                                       | string           | v1.8.1_vmware.1                                                                 |
| contour.image.pullPolicy                         | Contour image pull policy                                                                               | string           | IfNotPresent                                                                    |
| envoy.image.repository                           | Repository containing envoy image                                                                       | string           | registry.tkg.vmware.run                                                    |
| envoy.image.name                                 | Name of envoy image                                                                                     | string           | envoy                                                                           |
| envoy.image.tag                                  | Envoy image tag                                                                                         | string           | v1.15.0_vmware.1                                                                |
| envoy.image.pullPolicy                           | Envoy image pull policy                                                                                 | string           | IfNotPresent                                                                    |
| envoy.hostPort.enable                            | Flag to expose envoy ports on host                                                                      | boolean          | true                                                                           |
| envoy.hostPort.http                              | Envoy http host port                                                                                    | integer          | 80                                                                              |
| envoy.hostPort.https                             | Envoy https host port                                                                                   | integer          | 443                                                                             |
| envoy.service.type                               | Type of service to expose envoy. Supported Values: ClusterIP, NodePort, LoadBalancer                    | string           | vsphere: NodePort AWS: LoadBalancer                                             |
| envoy.service.externalTrafficPolicy              | External traffic policy of envoy service. Supported Values: Local, Cluster                              | string           | Cluster                                                                         |
| envoy.service.nodePort.http                      | Desired nodePort for service of type NodePort used for http requests                                    | integer          | Null - Kubernetes assigns a dynamic node port                                   |
| envoy.service.nodePort.https                     | Desired nodePort for service of type NodePort used for https requests                                   | integer          | Null - Kubernetes assigns a dynamic node port                                   |
| envoy.deployment.hostNetwork                     | Run envoy on hostNetwork                                                                                | boolean          | false                                                                           |
| envoy.service.aws.LBType                         | AWS LB type to be used for exposing envoy service. Supported Values: classic, nlb                       | string           | classic                                                                         |
| envoy.loglevel                                   | Log level to use for envoy                                                                              | string           | info                                                                            |

## Test Contour

### Envoy exposed using service type:NodePort

1. Deploy test pods and services

    ```sh
    kubectl apply -f ingress/examples/common/
    ```

2. Deploy kubernetes ingress resource

    ```sh
    kubectl apply -f ingress/examples/https-ingress/
    ```

3. Add /etc/hosts entry mapping one of the worker node IP to foo.bar.com

    ```sh
    echo '<WORKER_NODE_IP> foo.bar.com' | sudo tee -a /etc/hosts > /dev/null
    ```

4. Get Envoy service https port (ENVOY_SERVICE_HTTPS_PORT)

   By default the Envoy service https port will be 443. If you explicitly specify `envoy.hostPort.enable=false`, run the following command to get the random port assigned by Kubernetes:

    ```sh
    kubectl get service envoy -n tanzu-system-ingress -o jsonpath='{.spec.ports[?(@.name=="https")].nodePort}'
    ```

5. Verify if the following URLs work

    ```sh
    https://foo.bar.com:<ENVOY_SERVICE_HTTPS_PORT>/foo
    https://foo.bar.com:<ENVOY_SERVICE_HTTPS_PORT>/bar
    ```

### Envoy exposed using service type: LoadBalancer

1. Deploy test pods and services

    ```sh
    kubectl apply -f ingress/examples/common/
    ```

2. Deploy kubernetes ingress resource

    ```sh
    kubectl apply -f ingress/examples/https-ingress/
    ```

3. Get Envoy service loadbalancer hostname (ENVOY_SERVICE_LB_HOSTNAME)

    ```sh
    kubectl get service envoy -n tanzu-system-ingress -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
    ```

4. Get Envoy service loadbalancer ip (ENVOY_SERVICE_LB_IP)

    ```sh
    nslookup <ENVOY_SERVICE_LB_HOSTNAME>
    ```

5. Add /etc/hosts entry mapping Envoy service loadbalancer ip to foo.bar.com

    ```sh
    echo '<ENVOY_SERVICE_LB_IP> foo.bar.com' | sudo tee -a /etc/hosts > /dev/null
    ```

6. Verify if the following URLs work

    ```sh
    https://foo.bar.com/foo
    https://foo.bar.com/bar
    ```

## Troubleshooting

### Access Envoy admin interface remotely

1. Get one of the pods that matches the Envoy daemonset

    ```sh
    ENVOY_POD=$(kubectl -n tanzu-system-ingress get pod -l app=envoy -o name | head -1)
    ```

2. Port forward to the Envoy pod

    ```sh
    kubectl -n tanzu-system-ingress port-forward $ENVOY_POD 9001
    ```

3. Navigate to URL to access Envoy admin interface

   ```sh
   http://127.0.0.1:9001/
   ```

### Visualizing Contour's internal directed acyclic graph (DAG)

1. Get one of the Contour pods

    ```sh
    CONTOUR_POD=$(kubectl -n tanzu-system-ingress get pod -l app=contour -o name | head -1)
    ```

2. Port forward to the Contour pod

    ```sh
    kubectl -n tanzu-system-ingress port-forward $CONTOUR_POD 6060
    ```

3. Download and store the DAG in png format

    ```sh
    curl localhost:6060/debug/dag | dot -T png > contour-dag.png
    ```

4. Open contour-dag.png to view the graph
