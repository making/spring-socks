# Monitoring with prometheus

## Introduction

Prometheus is a system and service monitoring system. It collects metrics from configured targets at given intervals, evaluates rule expressions, displays the results, and can trigger alerts if some condition is observed to be true.
Prometheus will be deployed on workload cluster

### Upstream Repo Reference

<https://github.com/prometheus/prometheus>

<https://github.com/prometheus/alertmanager>

<https://github.com/kubernetes/kube-state-metrics>

<https://github.com/prometheus/node_exporter>

<https://github.com/prometheus/pushgateway>

### Monitoring spec

<https://confluence.eng.vmware.com/display/TKG/TKG+Monitoring+Spec#TKGMonitoringSpec-Proposal>

## Deploying Prometheus

### Prerequisites

* YTT installed (<https://github.com/k14s/ytt/releases>)
* Workload cluster deployed

### Assumptions

* Customer can choose either vsphere or aws or azure as infrastructure provider

### To deploy Prometheus on workload cluster

1. Create config.yaml file (Supported configurations key/value pairs are below)

   ```yaml
   #@data/values
   ---
   <key1>:<value1>
   <key2>:<value2>
   ```

2. Deploy Prometheus, Alertmanager and other monitoring components

   ```sh
   ytt --ignore-unknown-comments -f common/ -f monitoring/prometheus/ -f <config.yaml> -v infrastructure_provider="<infra_provider>" | kubectl apply -f-
   ```

   **Note**: `monitoring.create_namespace` is set to false by default. If the namespace has not been created prior to `kubectl apply`, the deployment will fail.
   Add `-v monitoring.create_namespace=true` in above command if you would like to create the namespace on the fly.

## Configurations

The default configuration values are in monitoring/prometheus/values.yaml

| Parameter                                                | Description                                                                                                  | Type             | Default                                                       |
|----------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|------------------|---------------------------------------------------------------|
| infrastructure_provider                                  | Infrastructure Provider. Supported Values: vsphere, aws, azure                                               | string           | vsphere / aws / azure                                         |
| monitoring.namespace                                     | Namespace where Prometheus will be deployed                                                                  | string           | tanzu-system-monitoring                                       |
| monitoring.create_namespace                              | The flag indicates whether to create the namespace specified by monitoring.namespace                         | boolean          | false                                                         |
| monitoring.prometheus_server.config.prometheus_yaml      | Kubernetes cluster monitor config details to be passed to Prometheus                                         | yaml file        | prometheus.yaml                                               |
| monitoring.prometheus_server.config.alerting_rules_yaml  | Detailed alert rules defined in Prometheus                                                                   | yaml file        | alerting_rules.yaml                                           |
| monitoring.prometheus_server.config.recording_rules_yaml | Detailed record rules defined in Prometheus                                                                  | yaml file        | recording_rules.yaml                                          |
| monitoring.prometheus_server.service.type                | Type of service to expose Prometheus. Supported Values: ClusterIP                                            | string           | ClusterIP                                                     |
| monitoring.prometheus_server.enable_alerts.kubernetes_api| Enable SLO alerting for the Kubernetes API in Prometheus                                                     | boolean          | true                                                          |
| monitoring.prometheus_server.sc.sc_enabled               | Define if storageclass is enabled in the deployment                                                          | boolean          | false                                                         |
| monitoring.prometheus_server.sc.is_default               | Define if current storageclass is default storageclass                                                       | boolean          | false                                                         |
| monitoring.prometheus_server.sc.vsphereDatastoreurl      | datastoreurl for storageclass used in vcenter                                                                | string           | "xxx-xxx-xxxx"                                                |
| monitoring.prometheus_server.sc.aws_type                 | AWS type defined for storageclass on AWS                                                                     | string           | gp2                                                           |
| monitoring.prometheus_server.sc.aws_fsType               | AWS file system type defined for storageclass on AWS                                                         | string           | ext4                                                          |
| monitoring.prometheus_server.sc.allowVolumeExpansion     | Define if volume expansion allowd for storageclass on AWS                                                    | boolean          | true                                                          |
| monitoring.prometheus_server.pvc.annotations             | Storage class annotations                                                                                    | map              | {}                                                            |
| monitoring.prometheus_server.pvc.storage_class           | Storage class to use for persistent volume claim. By default this is null and default provisioner is used    | string           | null                                                          |
| monitoring.prometheus_server.pvc.accessMode              | Define access mode for persistent volume claim. Supported values: ReadWriteOnce, ReadOnlyMany, ReadWriteMany | string           | ReadWriteOnce                                                 |
| monitoring.prometheus_server.pvc.storage                 | Define storage size for persistent volume claim                                                              | string           | 2Gi                                                           |
| monitoring.prometheus_server.deployment.replicas         | Number of prometheus replicas                                                                                | integer          | 1                                                             |
| monitoring.prometheus_server.image.repository            | Repository containing Prometheus image                                                                       | string           | registry.tkg.vmware.run/prometheus                                  |
| monitoring.prometheus_server.image.name                  | Name of Prometheus image                                                                                     | string           | prometheus                                                    |
| monitoring.prometheus_server.image.tag                   | Prometheus image tag                                                                                         | string           | v2.17.1_vmware.1                                              |
| monitoring.prometheus_server.image.pullPolicy            | Prometheus image pull policy                                                                                 | string           | IfNotPresent                                                  |
| monitoring.alertmanager.config.slack_demo                | Slack notification configuration for Alertmanager                                                            | string           | see slack config example                                      |
| monitoring.alertmanager.config.email_receiver            | Email notification configuration for Alertmanager                                                            | string           | see email config example                                      |
| monitoring.alertmanager.service.type                     | Type of service to expose Alertmanager. Supported Values: ClusterIP                                          | string           | ClusterIP                                                     |
| monitoring.alertmanager.image.repository                 | Repository containing Alertmanager image                                                                     | string           | registry.tkg.vmware.run/prometheus                                  |
| monitoring.alertmanager.image.name                       | Name of Alertmanager image                                                                                   | string           | alertmanager                                                  |
| monitoring.alertmanager.image.tag                        | Alertmanager image tag                                                                                       | string           | v0.20.0_vmware.1                                              |
| monitoring.alertmanager.image.pullPolicy                 | Alertmanager image pull policy                                                                               | string           | IfNotPresent                                                  |
| monitoring.alertmanager.pvc.annotations                  | Storage class annotations                                                                                    | map              | {}                                                            |
| monitoring.alertmanager.pvc.storage_class                | Storage class to use for persistent volume claim. By default this is null and default provisioner is used.   | string           | null                                                          |
| monitoring.alertmanager.pvc.accessMode                   | Define access mode for persistent volume claim. Supported values: ReadWriteOnce, ReadOnlyMany, ReadWriteMany | string           | ReadWriteOnce                                                 |
| monitoring.alertmanager.pvc.storage                      | Define storage size for persistent volume claim                                                              | string           | 2Gi                                                           |
| monitoring.alertmanager.deployment.replicas              | Number of alertmanager replicas                                                                              | integer          | 1                                                             |
| monitoring.kube_state_metrics.image.repository           | Repository containing kube-state-metircs image                                                               | string           | registry.tkg.vmware.run/prometheus                                  |
| monitoring.kube_state_metrics.image.name                 | Name of kube-state-metircs image                                                                             | string           | kube-state-metrics                                            |
| monitoring.kube_state_metrics.image.tag                  | kube-state-metircs image tag                                                                                 | string           | v1.9.5_vmware.1                                               |
| monitoring.kube_state_metrics.image.pullPolicy           | kube-state-metircs image pull policy                                                                         | string           | IfNotPresent                                                  |
| monitoring.kube_state_metrics.deployment.replicas        | Number of kube-state-metrics replicas                                                                        | integer          | 1                                                             |
| monitoring.node_exporter.image.repository                | Repository containing node-exporter image                                                                    | string           | registry.tkg.vmware.run/prometheus                                  |
| monitoring.node_exporter.image.name                      | Name of node-exporter image                                                                                  | string           | node-exporter                                                 |
| monitoring.node_exporter.image.tag                       | node-exporter image tag                                                                                      | string           | v0.18.1_vmware.1                                              |
| monitoring.node_exporter.image.pullPolicy                | node-exporter image pull policy                                                                              | string           | IfNotPresent                                                  |
| monitoring.node_exporter.deployment.replicas             | Number of node-exporter replicas                                                                             | integer          | 1                                                             |
| monitoring.pushgateway.image.repository                  | Repository containing pushgateway image                                                                      | string           | registry.tkg.vmware.run/prometheus                                  |
| monitoring.pushgateway.image.name                        | Name of pushgateway image                                                                                    | string           | pushgateway                                                   |
| monitoring.pushgateway.image.tag                         | pushgateway image tag                                                                                        | string           | v1.2.0_vmware.1                                               |
| monitoring.pushgateway.image.pullPolicy                  | pushgateway image pull policy                                                                                | string           | IfNotPresent                                                  |
| monitoring.pushgateway.deployment.replicas               | Number of pushgateway replicas                                                                               | integer          | 1                                                             |
| monitoring.cadvisor.image.repository                     | Repository containing cadvisor image                                                                         | string           | registry.tkg.vmware.run/prometheus                                  |
| monitoring.cadvisor.image.name                           | Name of cadvisor image                                                                                       | string           | cadvisor                                                      |
| monitoring.cadvisor.image.tag                            | cadvisor image tag                                                                                           | string           | v0.36.0_vmware.1                                              |
| monitoring.cadvisor.image.pullPolicy                     | cadvisor image pull policy                                                                                   | string           | IfNotPresent                                                  |
| monitoring.cadvisor.deployment.replicas                  | Number of cadvisor replicas                                                                                  | integer          | 1                                                             |
| monitoring.ingress.enabled                               | Enable/disable ingress for prometheus and alertmanager                                                       | boolean          | false                                                         |
| monitoring.ingress.virtual_host_fqdn                     | Hostname for accessing promethues and alertmanager                                                           | string           | prometheus.system.tanzu                                       |
| monitoring.ingress.prometheus_prefix                     | Path prefix for prometheus                                                                                   | string           | /                                                             |
| monitoring.ingress.alertmanager_prefix                   | Path prefix for alertmanager                                                                                 | string           | /alertmanager/                                                |
| monitoring.ingress.tlsCertificate.tls.crt                | Optional cert for ingress if you want to use your own TLS cert. A self signed cert is generated by default   | string           | Generated cert                                                |
| monitoring.ingress.tlsCertificate.tls.key                | Optional cert private key for ingress if you want to use your own TLS cert.                                  | string           | Generated cert key                                            |

slack & email configuration example

```yaml
slack_demo:
  name: slack_demo
  slack_configs:
  - api_url: https://hooks.slack.com
    channel: '#alertmanager-test'
```

```yaml
email_receiver:
  name: email-receiver
  email_configs:
  - to: demo@tanzu.com
    send_resolved: false
    from: from-email@tanzu.com
    smarthost: smtp.eample.com:25
    require_tls: false
```

### Configurable fields for Prometheus_server configmap

| Parameter             | Description                                                                                    | Type             | Default            |
|-----------------------|------------------------------------------------------------------------------------------------|------------------|--------------------|
| evaluation_interval   | frequency to evaluate rules                                                                    | duration         | 1m                 |
| scrape_interval       | frequency to scrape targets                                                                    | duration         | 1m                 |
| scrape_timeout        | How long until a scrape request times out                                                      | duration         | 10s                |
| rule_files            | Rule files specifies a list of globs. Rules and alerts are read from all matching files        | yaml file        |                    |
| scrape_configs        | A list of scrape configurations.                                                               | list             |                    |
| job_name              | The job name assigned to scraped metrics by default                                            | string           |                    |
| kubernetes_sd_configs | List of Kubernetes service discovery configurations.                                           | list             |                    |
| relabel_configs       | List of target relabel configurations.                                                         | list             |                    |
| action                | Action to perform based on regex matching.                                                     | string           |                    |
| regex                 | Regular expression against which the extracted value is matched.                               | string           |                    |
| source_labels         | The source labels select values from existing labels.                                          | string           |                    |
| scheme                | Configures the protocol scheme used for requests.                                              | string           |                    |
| tls_config            | Configures the scrape request's TLS settings.                                                  | string           |                    |
| ca_file               | CA certificate to validate API server certificate with.                                        | filename         |                    |
| insecure_skip_verify  | Disable validation of the server certificate.                                                  | boolean          |                    |
| bearer_token_file     | Optional bearer token file authentication information.                                         | filename         |                    |
| replacement           | Replacement value against which a regex replace is performed if the regular expression matches | string           |                    |
| target_label          | Label to which the resulting value is written in a replace action.                             | string           |                    |

### Configurable fields for Alertmanager configmap

| Parameter             | Description                                                                                                | Type             | Default              |
|-----------------------|------------------------------------------------------------------------------------------------------------|------------------|----------------------|
| resolve_timeout       | ResolveTimeout is the default value used by alertmanager if the alert does not include EndsAt              | duration         | 5m                   |
| smtp_smarthost        | The SMTP host through which emails are sent.                                                               | duration         | 1m                   |
| slack_api_url         | The Slack webhook URL.                                                                                     | string           | global.slack_api_url |
| pagerduty_url         | The pagerduty URL to send API requests to                                                                  | string           | global.pagerduty_url |
| templates             | Files from which custom notification template definitions are read                                         | file path        |                      |
| group_by              | group the alerts by label                                                                                  | string           |                      |
| group_interval        | set time to wait before sending a notification about new alerts that are added to a group                  | duration         | 5m                   |
| group_wait            | How long to initially wait to send a notification for a group of alerts                                    | duration         | 30s                  |
| repeat_interval       | How long to wait before sending a notification again if it has already been sent successfully for an alert | duration         | 4h                   |
| receivers             | A list of notification receivers.                                                                          | list             |                      |
| severity              | Severity of the incident.                                                                                  | string           |                      |
| channel               | The channel or user to send notifications to.                                                              | string           |                      |
| html                  | The HTML body of the email notification.                                                                   | string           |                      |
| text                  | The text body of the email notification.                                                                   | string           |                      |
| send_resolved         | Whether or not to notify about resolved alerts.                                                            | filename         |                      |
| email_configs         | Configurations for email integration                                                                       | boolean          |                      |

### Per-pod Prometheus Annotations

Annotations on pods allow a fine control of the scraping process:

* `<prometheus.io/scrape>` : The default configuration will scrape all pods and, if set to false, this annotation will exclude the pod from the scraping process.

* `<prometheus.io/path>`: If the metrics path is not /metrics, define it with this annotation.

* `<prometheus.io/port>`: Scrape the pod on the indicated port instead of the pod’s declared ports (default is a port-free target if none are declared).

These annotations need to be part of the pod metadata. They will have no effect if set on other objects such as Services or DaemonSets.

The DaemonSet manifest below will instruct Prometheus to scrape all of its pods on port 9102.

```sh
apiVersion: apps/v1beta2 # for versions before 1.8.0 use extensions/v1beta1
kind: DaemonSet
metadata:
  name: fluentd-elasticsearch
  namespace: weave
  labels:
    app: fluentd-logging
spec:
  selector:
    matchLabels:
      name: fluentd-elasticsearch
  template:
    metadata:
      labels:
        name: fluentd-elasticsearch
      annotations:
        prometheus.io/scrape: 'true'
        prometheus.io/port: '9102'
    spec:
      containers:
      - name: fluentd-elasticsearch
        image: gcr.io/google-containers/fluentd-elasticsearch:1.20
```

### Install Prometheus service with ingress enabled or disabled

1. Install cert-manager on the Workload Cluster if not already installed.

    ```sh
    kubectl apply -f cert-manager/
    ```

2. Install Contour on the Workload Cluster if not already installed. Follow [Contour's README](../../ingress/contour/README.md) to install Contour.

3. Install Prometheus
    * Ingress disabled

    ```sh
    ytt --ignore-unknown-comments -f common/ -f monitoring/prometheus/ -v infrastructure_provider=vsphere | kubectl apply -f -
    ```

   * Ingress enabled

   ```sh
   ytt --ignore-unknown-comments -f common/ -f monitoring/prometheus/ -v infrastructure_provider=vsphere -v monitoring.ingress.enabled=true | kubectl apply -f -
   ```

Update your local /etc/hosts to point prometheus.system.tanzu to either LoadBalancer(AWS, Azure) or a worker node IP(vSphere) to access prometheus.
