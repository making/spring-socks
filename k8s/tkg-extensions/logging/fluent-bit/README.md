# Logging with fluent bit

## Introduction

Fluent bit is a fast and lightweight log processor and forwarder which allows you to collect data/logs from different sources, unify and send them to multiple destinations.
Fluent bit will be deployed on the management cluster and workload cluster as a log shipper

## Upstream Repo Reference

<https://github.com/fluent/fluent-bit-kubernetes-logging/commit/1cdfc96be5c265364095d5b3525c5d992a320aa9>

### Changes made

* Remove deprecated API versions in favor of newer and more stable API versions
* Introduce spec.selector which is now required

## Logging spec

<http://go/tkg-logging-spec>

## Setup for vSphere 6.7u3/7.0 and AWS

### Prerequisites

* YTT installed (<https://github.com/k14s/ytt/releases>)
* Management cluster deployed or
* Workload cluster deployed

### Assumptions

* Fluent bit is used only as a log shipper from management cluster or workload cluster
* Customer should have logging management backend deployed for storing and analyzing logs
* Customer can choose only one of the supported logging backend at one time
* Logging with fluent bit supports the following fluent bit output plugins:
  * Elastic Search
  * Kafka
  * Splunk
  * HTTP

### To deploy fluent bit on management cluster or workload cluster

1. Create config.yaml file (Supported configurations key/value pairs are highlighted below in Configurations section

   ```yaml
   #@data/values
   ---
   <key1>:<value1>
   <key2>:<value2>
   ```

#### Follow one of the following 4 sections depending upon your logging backend

#### Deploy fluent bit with Elastic Search Output Plugin

1. Deploy fluent bit with configuration parameters for elastic search output plugin

   ```sh
   ytt  -f common/ -f logging/fluent-bit/ -f <config.yaml> -v fluent_bit.elasticsearch.host='<fluent_elasticsearch_host>' -v fluent_bit.elasticsearch.port='<fluent_elasticsearch_port>' -v tkg.cluster_name='<tkg_cluster_name>' -v tkg.instance_name='<tkg_instance_name>' -v fluent_bit.output_plugin='<fluent_output_plugin>' | kubectl apply -f-
   ```

#### Deploy fluent bit with Kafka Output Plugin

1. Deploy fluent bit with configuration parameters for kafka output plugin

   ```sh
   ytt  -f common/ -f logging/fluent-bit/ -f <config.yaml> -v fluent_bit.kafka.broker_service_name='<kafka_broker_service_name>' -v fluent_bit.kafka.topic_name='<kafka_topic_name>' -v tkg.cluster_name='<tkg_cluster_name>' -v tkg.instance_name='<tkg_instance_name>' -v fluent_bit.output_plugin='<fluent_output_plugin>' | kubectl apply -f-
   ```

#### Deploy fluent bit with Splunk Output Plugin

1. Deploy fluent bit with configuration parameters for splunk output plugin

   ```sh
   ytt  -f common/ -f logging/fluent-bit/ -f <config.yaml> -v fluent_bit.splunk.host='<splunk_host>' -v fluent_bit.splunk.port='<splunk_host>' -v fluent_bit.splunk.token='<splunk_token>' -v tkg.cluster_name='<tkg_cluster_name>' -v tkg.instance_name='<tkg_instance_name>' -v fluent_bit.output_plugin='<fluent_output_plugin>' | kubectl apply -f-
   ```

#### Deploy fluent bit with HTTP Output Plugin

1. Deploy fluent bit with configuration parameters for HTTP output plugin

   ```sh
   ytt  -f common/ -f logging/fluent-bit/ -f <config.yaml> -v fluent_bit.http.port='<http_port>' -v fluent_bit.http.header_key_value='<http_header_key_value>' -v fluent_bit.http.host='<http_host>' -v fluent_bit.http.uri='<http_uri>' -v fluent_bit.http.format='<http_format>' -v tkg.cluster_name='<tkg_cluster_name>' -v tkg.instance_name='<tkg_instance_name>' -v fluent_bit.output_plugin='<fluent_output_plugin>' | kubectl apply -f-
   ```

 This would create all the following components:

 1. Logging namespace
 2. RBAC resources for fluent bit, so that Fluent Bit can access the appropriate components
    * Service Account
    * Cluster Role, which grants get, list, and watch permissions on pods and namespace objects
    * Cluster Role Binding, which binds the ClusterRole to the ServiceAccount within the logging namespace
 3. Fluent Bit Config Map
 4. Fluent Bit DaemonSet

### Configurations

The default configuration values are in logging/fluent-bit/values.yaml

| Parameter                               | Description                                                                                             | Type             | Default                                                            |
|-----------------------------------------|---------------------------------------------------------------------------------------------------------|------------------|--------------------------------------------------------------------|
| logging.namespace                       | Namespace where fluent bit will be deployed                                                             | string           | tanzu-system-logging                                               |
| logging.service_account_name            | Name of fluent bit service account                                                                      | string           | fluent-bit                                                         |
| logging.cluster_role_name               | Name of cluster role which grants get, watch and list permissions to fluent bit                         | string           | fluent-bit-read                                                    |
| logging.image.name                      | Name of fluent bit image                                                                                | string           | fluent-bit                                                         |
| logging.image.tag                       | Fluent bit image tag                                                                                    | string           | v1.3.8_vmware.1                                                    |
| logging.image.repository                | Repository containing fluent bit image                                                                  | string           | registry.tkg.vmware.run                                       |
| logging.image.pullPolicy                | Fluent bit image pull policy                                                                            | string           | IfNotPresent                                                       |
| logging.update_strategy                 | Update strategy to be used when updating DaemonSet                                                      | string           | RollingUpdate                                                      |
| tkg.cluster_name                        | Name of the tkg workload cluster                                                                        | string           | Null ( Mandatory parameter )                                       |
| tkg.instance_name                       | Name of tkg instance, shared by management cluster and all the workload clusters in one TKG deployment  | string           | Null ( Mandatory parameter )                                       |
| fluent_bit.log_level                    | Log level to use for fluent bit                                                                         | string           | info                                                               |
| fluent_bit.output_plugin                | Set the backend to which Fluent-Bit should flush the information it gathers                             | string           | Null ( Mandatory parameter )                                       |
| fluent_bit.elasticsearch.host           | IP address or hostname of the target Elasticsearch instance                                             | string           | Null ( Mandatory parameter when output_plugin is elastic search )  |
| fluent_bit.elasticsearch.port           | TCP port of the target Elasticsearch instance                                                           | integer          | Null ( Mandatory parameter when output_plugin is elastic search)   |
| fluent_bit.kafka.broker_service_name    | Single of multiple list of Kafka Brokers, e.g: 192.168.1.3:9092                                         | string           | Null ( Mandatory parameter when output_plugin is kafka)            |
| fluent_bit.kafka.topic_name             | Single entry or list of topics separated by (,) that Fluent Bit will use to send messages to Kafka      | string           | Null ( Mandatory parameter when output_plugin is kafka)            |
| fluent_bit.splunk.host                  | IP address or hostname of the target Splunk Server                                                      | string           | Null ( Mandatory parameter when output_plugin is splunk)           |
| fluent_bit.splunk.port                  | TCP port of the target Splunk Server                                                                    | integer          | Null ( Mandatory parameter when output_plugin is splunk)           |
| fluent_bit.splunk.token                 | Specify the Authentication Token for the HTTP Event Collector interface                                 | string           | Null ( Mandatory parameter when output_plugin is splunk)           |
| fluent_bit.http.host                    | IP address or hostname of the target HTTP Server                                                        | string           | Null ( Mandatory parameter when output_plugin is http)             |
| fluent_bit.http.port                    | TCP port of the target HTTP Server                                                                      | integer          | Null ( Mandatory parameter when output_plugin is http)             |
| fluent_bit.http.header_key_value        | HTTP header key/value pair. Multiple headers can be set.                                                | string           | Null ( Mandatory parameter when output_plugin is http)             |
| fluent_bit.http.format                  | Specify the data format to be used in the HTTP request body                                             | string           | Null ( Mandatory parameter when output_plugin is http)             |
| host_path.volume_1                      | Directory path from the host node's filesystem into the pod, for volume 1                               | string           | /var/log                                                           |
| host_path.volume_2                      | Directory path from the host node's filesystem into the pod, for volume 2                               | string           | /var/lib/docker/containers                                         |
| host_path.volume_3                      | Directory path from the host node's filesystem into the pod, for volume 3                               | string           | /run/log                                                           |
