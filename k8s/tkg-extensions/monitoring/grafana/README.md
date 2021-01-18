# Monitoring with Grafana

## Introduction

Grafana is open source visualization and analytics software. It allows you to query, visualize, alert on, and explore your metrics no matter where they are stored. In plain English, it provides you with tools to turn your time-series database (TSDB) data into beautiful graphs and visualizations.

### Upstream Repo Reference

<https://github.com/grafana/grafana>

<https://github.com/helm/charts/tree/master/stable/grafana>

#### Changes made

### Monitoring spec

<https://confluence.eng.vmware.com/display/TKG/TKG+Monitoring+Spec#TKGMonitoringSpec-Proposal>

### vSphere 6.7u3 setup

#### Prerequisites

* YTT installed (<https://github.com/k14s/ytt/releases>)
* Workload cluster deployed

### Assumptions

* Customer can choose either vsphere or aws or azure as infrastructure provider

### To deploy Grafana on workload cluster

1. Create config.yaml file (Supported configurations key/value pairs are below)

   ```yaml
   #@data/values
   ---
   <key1>:<value1>
   <key2>:<value2>
   ```

2. Deploy Grafana on workload cluster

   ```sh
   ytt --ignore-unknown-comments -f common/ -f monitoring/grafana/ -f <config.yaml> -v monitoring.grafana.secret.admin_password="<admin_password>" -v infrastructure_provider="<infra_provider>" | kubectl apply -f-
   ```

   **Note**: `monitoring.create_namespace` is set to false by default. If the namespace has not been created prior to `kubectl apply`, the deployment will fail.
   Add `-v monitoring.create_namespace=true` in above command if you would like to create the namespace on the fly.

## Configurations

The default configuration values are in monitoring/grafana/values.yaml

| Parameter                                          | Description                                                                                                  | Type             | Default                                                  |
|----------------------------------------------------|--------------------------------------------------------------------------------------------------------------|------------------|----------------------------------------------------------|
| infrastructure_provider                            | Infrastructure Provider. Supported Values: vsphere, aws, azure                                               | string           | vsphere / aws / azure                                    |
| monitoring.namespace                               | Namespace where Prometheus will be deployed                                                                  | string           | tanzu-system-monitoring                                  |
| monitoring.create_namespace                        | The flag indicates whether to create the namespace specified by monitoring.namespace                         | boolean          | false                                                    |
| monitoring.grafana.cluster_role.apiGroups          | api group defined for grafana clusterrole                                                                    | list             | [""]                                                     |
| monitoring.grafana.cluster_role.resources          | resources defined for grafana clusterrole                                                                    | list             | ["configmaps", "secrets"]                                |
| monitoring.grafana.cluster_role.verbs              | access permission defined for clusterrole                                                                    | list             | ["get", "watch", "list"]                                 |
| monitoring.grafana.config.grafana_ini              | Grafana configuration file details                                                                           | config file      | grafana.ini                                              |
| monitoring.grafana.config.datasource.type          | Grafana datasource type                                                                                      | string           | prometheus                                               |
| monitoring.grafana.config.datasource.access        | access mode. proxy or direct (Server or Browser in the UI)                                                   | string           | proxy                                                    |
| monitoring.grafana.config.datasource.isDefault     | mark as default Grafana datasource                                                                           | boolean          | true                                                     |
| monitoring.grafana.config.provider_yaml            | Config file to define grafana dashboard provider                                                             | yaml file        | provider.yaml                                            |
| monitoring.grafana.service.type                    | Type of service to expose Grafana. Supported Values: ClusterIP, NodePort, LoadBalancer                       | string           | vSphere: NodePort, aws/azure: LoadBalancer               |
| monitoring.grafana.pvc.storage_class               | Storage class to use for persistent volume claim. By default this is null and default provisioner is used    | string           | null                                                     |
| monitoring.grafana.pvc.accessMode                  | Define access mode for persistent volume claim. Supported values: ReadWriteOnce, ReadOnlyMany, ReadWriteMany | string           | ReadWriteOnce                                            |
| monitoring.grafana.pvc.storage                     | Define storage size for persistent volume claim                                                              | string           | 2Gi                                                      |
| monitoring.grafana.deployment.replicas             | Number of grafana replicas                                                                                   | integer          | 1                                                        |
| monitoring.grafana.image.repository                | Repository containing Grafana image                                                                          | string           | registry.tkg.vmware.run/grafana                             |
| monitoring.grafana.image.name                      | Name of Grafana image                                                                                        | string           | grafana                                                  |
| monitoring.grafana.image.tag                       | Grafana image tag                                                                                            | string           | v7.0.3_vmware.1                                          |
| monitoring.grafana.image.pullPolicy                | Grafana image pull policy                                                                                    | string           | IfNotPresent                                             |
| monitoring.grafana.secret.type                     | Secret type defined for Grafana dashboard                                                                    | string           | Opaque                                                   |
| monitoring.grafana.secret.admin_user               | username to access Grafana dashboard                                                                         | string           | YWRtaW4=                                                 |
| monitoring.grafana.secret.admin_password           | password to access Grafana dashboard                                                                         | string           | null                                                     |
| monitoring.grafana.secret.ldap_toml                | If using ldap auth, ldap configuration file path                                                             | string           | ""                                                       |
| monitoring.grafana_init_container.image.repository | Repository containing grafana init container image                                                           | string           | registry.tkg.vmware.run/grafana                                                 |
| monitoring.grafana_init_container.image.name       | Name of grafana init container image                                                                         | string           | k8s-sidecar                                              |
| monitoring.grafana_init_container.image.tag        | grafana init container image tag                                                                             | string           | 0.1.99                                                   |
| monitoring.grafana_init_container.image.pullPolicy | grafana init container image pull policy                                                                     | string           | IfNotPresent                                             |                                                                    |
| monitoring.grafana_sc_dashboard.image.repository   | Repository containing grafana dashboard image                                                                | string           | registry.tkg.vmware.run/grafana                                                 |
| monitoring.grafana_sc_dashboard.image.name         | Name of grafana dashboard image                                                                              | string           | k8s-sidecar                                              |
| monitoring.grafana_sc_dashboard.image.tag          | grafana dashboard image tag                                                                                  | string           | 0.1.99                                                   |
| monitoring.grafana_sc_dashboard.image.pullPolicy   | grafana dashboard image pull policy                                                                          | string           | IfNotPresent                                             |
| monitoring.grafana.ingress.enabled                 | Enable/disable ingress for grafana                                                                           | boolean          | true                                                     |
| monitoring.grafana.ingress.virtual_host_fqdn       | Hostname for accessing grafana                                                                               | string           | grafana.system.tanzu                                     |
| monitoring.grafana.ingress.prefix                  | Path prefix for grafana                                                                                      | string           | /                                                        |
| monitoring.grafana.ingress.tlsCertificate.tls.crt  | Optional cert for ingress if you want to use your own TLS cert. A self signed cert is generated by default   | string           | Generated cert                                           |
| monitoring.grafana.ingress.tlsCertificate.tls.key  | Optional cert private key for ingress if you want to use your own TLS cert.                                  | string           | Generated cert key                                       |

### Note

1. `monitoring.grafana.secret.admin_user` default value is base64 encoded, to decode, use `echo "xxxxxx" | base64 --decode`

2. In grafana.ini, grafana_net url is used for integrate with Grafana. For example, import dashboard directly from Grafana.com

### Deploy grafana example

1. Install cert-manager on the Workload Cluster if not already installed.

    ```sh
    kubectl apply -f cert-manager/
    ```

2. Install Contour on the Workload Cluster if not already installed. Follow [Contour's README](../../ingress/contour/README.md) to install Contour.

3. Install Grafana
    * Ingress enabled

    ```sh
    ytt --ignore-unknown-comments -f common/ -f monitoring/grafana/ -v monitoring.grafana.secret.admin_password="<admin_password>" -v infrastructure_provider=vsphere | kubectl apply -f-
    ```

   * Ingress disabled

   ```sh
   ytt --ignore-unknown-comments -f common/ -f monitoring/grafana/ -v monitoring.grafana.secret.admin_password="<admin_password>" -v infrastructure_provider=vsphere monitoring.grafana.ingress.enabled=false | kubectl apply -f-
   ```

Update your local /etc/hosts to point grafana.system.tanzu to either LoadBalancer(AWS, Azure) or a worker node IP(vSphere) to access grafana.
