# Upgrade TKG Extensions from Alexandria to Beijing

## Introduction

In Alexandria release, TKG extensions were deployed by applying the extensions manifests using kubectl.
In Beijing release, TKG extensions will be deployed and managed using TMC's extension manager and k14's kapp-controller.
Since the mechanism to deploy TKG extensions is drastically different in Beijing release, upgrade of these TKG extensions will be a disruptive operation i.e.
TKG extensions will have to be deleted and recreated using the new mechanism. This documentation provides steps on how to save existing TKG extensions configurations and apply them using the
new mechanism.

### Prerequisites

* YTT installed (<https://github.com/k14s/ytt/releases>).
* Workload cluster deployed.

#### Contour

1. Get contour configmap

    ```sh
    kubectl get configmap contour -n tanzu-system-ingress -o jsonpath='{.data.contour\.yaml}' > contour-config.yaml
    ```

    `contour-config.yaml` which has **request-timeout** and **disablePermitInsecure** configured

    ```yaml
    # should contour expect to be running inside a k8s cluster
    # incluster: true
    #
    # path to kubeconfig (if not running inside a k8s cluster)
    # kubeconfig: /path/to/.kube/config
    #
    # Client request timeout to be passed to Envoy
    # as the connection manager request_timeout.
    # Defaults to 0, which Envoy interprets as disabled.
    # Note that this is the timeout for the whole request,
    # not an idle timeout.
    request-timeout: 10s
    # disable ingressroute permitInsecure field
    disablePermitInsecure: true

    <output trimmed>
    ```

2. If using vsphere, get the current NodePorts assigned

     ```sh
     kubectl get svc envoy -n tanzu-system-ingress -o jsonpath='{.spec.ports[?(@.name=="http")].nodePort}'
     kubectl get svc envoy -n tanzu-system-ingress -o jsonpath='{.spec.ports[?(@.name=="https")].nodePort}'
     ```

3. Delete contour namespace

   ```sh
   kubectl delete namespace tanzu-system-ingress
   kubectl delete clusterrolebinding contour
   kubectl delete clusterrole contour
   ```

4. Make sure ingress and http proxy resource are still intact

   ```sh
   kubectl get ingress -A
   kubectl get httpproxy -A
   ```

   Since we have deleted contour, the ingress traffic will not work. Once contour is upgraded,
   the traffic should again start working

5. Deploy new contour following [deploy-contour-extension](ingress/contour/README.md) until Step 5.

   After copying contour-data-values.yaml.example to contour-data-values.yaml, add the configurations from contour-config.yaml saved in Step 1
   and add nodePort saved in Step 2 to contour-data-values.yaml.

   Data values configurations are documented in [contour-configurations](../ingress/contour/README.md)
   Contour's configmap configurations will be under contour.config section in contour-data-values.yaml

   The below example shows how request-timeout and disablePermitInsecure options present in existing contour config and
   node ports can be added to contour-data-values.yaml

    ```yaml
    #@data/values
    #@overlay/match-child-defaults missing_ok=True
    ---
    infrastructure_provider: "<INFRA_PROVIDER>"
    contour:
      dummykey: "dummyvalue"
      config:
        disablePermitInsecure: true
        timeouts:
          requestTimeout: 10s
    envoy:
      service:
        nodePort:
          http: <ENVOY_SVC_HTTP_NODE_PORT>
          https: <ENVOY_SVC_HTTPS_NODE_PORT>
    ```

6. Generate the manifests to ensure the manifests are as expected

    ```sh
     ytt --ignore-unknown-comments -f ../common -f ../ingress/contour -f ingress/contour/<INFRA_PROVIDER>/contour-data-values.yaml
    ```

7. Follow steps from Step 6 in [deploy-contour-extension](ingress/contour/README.md) to deploy contour

8. Ensure ingress and httpproxy resources are valid and ingress traffic works

    ```sh
    kubectl get ingress -A
    kubectl get httpproxy -A
    ```

#### Fluent Bit

1. For Alexandria and Beijing release, Fluent bit supports four different output plugins. In order to upgrade, perform either of step 1(a), 1(b), 1(c) or 1(d) depending on output plugin in use

    (a) Elastic Search Output Plugin

    Get fluent-bit configmap

      ```sh
      kubectl get configmap fluent-bit-config -n tanzu-system-logging -o jsonpath='{.data.output-elasticsearch\.conf}' > fluent-bit-config-plugin.yaml
      kubectl get configmap fluent-bit-config -n tanzu-system-logging -o jsonpath='{.data.filter-record\.conf}' > fluent-bit-config-filter.yaml
      ```

    `output-elasticsearch.conf` which has **Host** and **Port** configured

      ```none
      [OUTPUT]
        Name            es
        Match           *
        Host            elasticsearch
        Port            9200
      <output trimmed>
      ```

    `filter-record.conf` which has **tkg_cluster** and **tkg_instance** configured

      ```none
      [FILTER]
        Name                record_modifier
        Match               *
        Record tkg_cluster  tkg-wc-1
        Record tkg_instance tkg-mc-1
      ```

    (b) Kafka Output Plugin

    Get fluent-bit configmap

      ```sh
      kubectl get configmap fluent-bit-config -n tanzu-system-logging -o jsonpath='{.data.output-kafka\.conf}' > fluent-bit-config-plugin.yaml
      kubectl get configmap fluent-bit-config -n tanzu-system-logging -o jsonpath='{.data.filter-record\.conf}' > fluent-bit-config-filter.yaml
      ```

    `output-kafka.conf` which has **Brokers** and **Topics** configured

      ```none
      [OUTPUT]
        Name           kafka
        Match          *
        Brokers        kafka-service:9092
        Topics         tkg-logs
      <output trimmed>
      ```

    `filter-record.conf` which has **tkg_cluster** and **tkg_instance** configured

      ```none
      [FILTER]
        Name                record_modifier
        Match               *
        Record tkg_cluster  tkg-wc-1
        Record tkg_instance tkg-mc-1
      ```

    (c) Splunk Output Plugin

    Get fluent-bit configmap

      ```sh
      kubectl get configmap fluent-bit-config -n tanzu-system-logging -o jsonpath='{.data.output-splunk\.conf}' > fluent-bit-config-plugin.yaml
      kubectl get configmap fluent-bit-config -n tanzu-system-logging -o jsonpath='{.data.filter-record\.conf}' > fluent-bit-config-filter.yaml
      ```

    `output-splunk.conf` which has **Host**, **Port** and **Splunk_Token** configured

      ```none
      [OUTPUT]
        Name           splunk
        Match          *
        Host           example-splunk-host
        Port           8088
        Splunk_Token   foo-bar
      <output trimmed>
      ```

    `filter-record.conf` which has **tkg_cluster** and **tkg_instance** configured

      ```none
      [FILTER]
        Name                record_modifier
        Match               *
        Record tkg_cluster  tkg-wc-1
        Record tkg_instance tkg-mc-1
      ```

    (d) HTTP Output Plugin

    Get fluent-bit configmap

      ```sh
      kubectl get configmap fluent-bit-config -n tanzu-system-logging -o jsonpath='{.data.output-http\.conf}' > fluent-bit-config-plugin.yaml
      kubectl get configmap fluent-bit-config -n tanzu-system-logging -o jsonpath='{.data.filter-record\.conf}' > fluent-bit-config-filter.yaml
      ```

    `output-http.conf` which has **Host**, **Port**, **URI**, **Header** and **Format** configured

      ```none
      [OUTPUT]
        Name              http
        Match             *
        Host              example-http-host
        Port              9200
        URI               /foo/bar
        Header            Authorization Bearer Token
        Format            json
      <output trimmed>
      ```

    `filter-record.conf` which has **tkg_cluster** and **tkg_instance** configured

      ```none
      [FILTER]
        Name                record_modifier
        Match               *
        Record tkg_cluster  tkg-wc-1
        Record tkg_instance tkg-mc-1
      ```

2. Delete fluent-bit namespace

   ```sh
   kubectl delete namespace tanzu-system-logging
   kubectl delete clusterrolebinding fluent-bit-read
   kubectl delete clusterrole fluent-bit-read
   ```

   Since we have deleted namespace, fluent bit daemonset is also deleted. Logs will not be captured for this time. Once fluent bit is upgraded, the log collection should again start working

3. Deploy new fluent-bit following [deploy-fluent-bit-extension](logging/fluent-bit/README.md) until Step 5.

   After copying fluent-bit-data-values.yaml.example to fluent-bit-data-values.yaml for <LOG_BACKEND> which is being used, add the configurations from fluent-bit-config-plugin.conf and fluent-bit-config-filter.conf saved in Step 1
   to fluent-bit-data-values.yaml.

   Data values configurations are documented in [fluent-bit-configurations](../logging/fluent-bit/README.md)

   Follow either of (a), (b), (c) or (d) section depending upon output plugin being used

   (a) Elastic Search Output Plugin

   Fluent bit's config map configurations for elastic search will be under fluent_bit.elasticsearch section in fluent-bit-data-values.yaml

   The below example shows how elastic search host and port options present in existing fluent-bit config can be added to fluent-bit-data-values.yaml

    ```yaml
    #@data/values
    #@overlay/match-child-defaults missing_ok=True
    ---

    tkg:
      instance_name: "tkg-mc-1"
      cluster_name: "tkg-wc-1"
    fluent_bit:
      output_plugin: "elasticsearch"
      elasticsearch:
        host: "elasticsearch"
        port: "9200"
    ```

   (b) Kafka Output Plugin

   Fluent bit's config map configurations for kafka will be under fluent_bit.kafka section in fluent-bit-data-values.yaml

   The below example shows how kafka broker service name and topic name options present in existing fluent-bit config can be added to fluent-bit-data-values.yaml

    ```yaml
    #@data/values
    #@overlay/match-child-defaults missing_ok=True
    ---

    tkg:
     instance_name: "tkg-mc-1"
     cluster_name: "tkg-wc-1"
    fluent_bit:
     output_plugin: "kafka"
     kafka:
       broker_service_name: "kafka-service:9092"
       topic_name: "tkg-logs"
    ```

   (c) Splunk Output Plugin

   Fluent bit's config map configurations for kafka will be under fluent_bit.splunk section in fluent-bit-data-values.yaml

   The below example shows how splunk host, port and token options present in existing fluent-bit config can be added to fluent-bit-data-values.yaml

    ```yaml
    #@data/values
    #@overlay/match-child-defaults missing_ok=True
    ---

    tkg:
     instance_name: "mgmt-cluster-1"
     cluster_name: "workload-1"
    fluent_bit:
     output_plugin: "splunk"
     splunk:
       host: "example-splunk-host"
       port: "8088"
       token: "foo-bar"
    ```

   (d) HTTP Output Plugin

   Fluent bit's config map configurations for kafka will be under fluent_bit.http section in fluent-bit-data-values.yaml

   The below example shows how http host, port, uri, header_key_value and format options present in existing fluent-bit config can be added to fluent-bit-data-values.yaml

    ```yaml
    #@data/values
    #@overlay/match-child-defaults missing_ok=True
    ---

    tkg:
    instance_name: "mgmt-cluster-1"
    cluster_name: "workload-1"
    fluent_bit:
      output_plugin: "http"
      http:
        host: "example-http-host"
        port: "9200"
        uri: "/foo/bar"
        header_key_value: "Authorization Bearer Token"
        format: "json"
    ```

4. Generate the manifests to ensure the manifests are as expected

    ```sh
     ytt --ignore-unknown-comments -f ../common -f ../logging/fluent-bit -f logging/fluent-bit/<LOG_BACKEND>/fluent-bit-data-values.yaml
    ```

5. Follow steps from Step 6 in [deploy-fluent-bit-extension](logging/fluent-bit/README.md) to deploy fluent-bit

6. Ensure fluent bit daemon set is ready and log collection and forwarding works

    ```sh
    kubectl get ds -n tanzu-system-logging
    ```
