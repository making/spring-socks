load("@ytt:data", "data")
load("@ytt:assert", "assert")
load("/globals.star", "globals")
load("/globals.star", "validate_infrastructure_provider")

SERVICE_TYPE_NODEPORT = "NodePort"
SERVICE_TYPE_LOADBALANCER = "LoadBalancer"

def get_service_type():
  if globals.infrastructure_provider == "vsphere":
    return SERVICE_TYPE_NODEPORT
  else:
    return SERVICE_TYPE_LOADBALANCER
  end
end

def is_service_type_LB():
  return get_prometheus_server_service_type() == SERVICE_TYPE_LOADBALANCER
end

def get_prometheus_server_service_type():
  if data.values.monitoring.prometheus_server.service.type:
    return data.values.monitoring.prometheus_server.service.type
  else:
    return get_service_type()
  end
end

def get_prometheus_server_service_annotations():
  annotations = {}
  if globals.infrastructure_provider == "aws":
    annotations["service.beta.kubernetes.io/aws-load-balancer-backend-protocol"] = "tcp"
  end
  return annotations
end

def validate_monitoring_namespace():
  # Namespace checking
  data.values.monitoring.namespace or assert.fail("missing monitoring.namespace")
end

def validate_prometheus_server():
  validate_funcs = [validate_monitoring_namespace,
                    validate_prometheus_server_sc_datastoreurl,
                    validate_prometheus_server_deployment,
                    validate_prometheus_server_image,
                    validate_prometheus_server_rbac_component_names]
  for validate_func in validate_funcs:
    validate_func()
  end
end

def validate_prometheus_server_deployment():
  data.values.monitoring.prometheus_server.deployment.replicas or assert.fail("Prometheus deployment replicas should be provided")
end

def validate_prometheus_server_image():
  # Image Name and version checking
  data.values.monitoring.prometheus_server.image.name or assert.fail("missing prometheus_server.image.name")
  data.values.monitoring.prometheus_server.image.tag or assert.fail("missing prometheus_server.image.tag")
  data.values.monitoring.prometheus_server.image.repository or assert.fail("missing prometheus_server.image.repository ")
  data.values.monitoring.prometheus_server.image.pullPolicy or assert.fail("missing prometheus_server.image.pullPolicy")
end

def validate_prometheus_server_rbac_component_names():
  data.values.monitoring.prometheus_server.service_account_name or assert.fail("missing prometheus_server.service_account_name")
  data.values.monitoring.prometheus_server.cluster_role_name or assert.fail("missing prometheus_server.cluster_role_name")
end

def validate_prometheus_server_sc_datastoreurl():
  if globals.infrastructure_provider == "vsphere" and data.values.monitoring.prometheus_server.sc.sc_enabled == True:
    data.values.monitoring.prometheus_server.sc.vsphereDatastoreurl or assert.fail("missing prometheus storage class datastoreurl")
  end
end

def labels_prometheus_server():
  return { "component": "server", "app": "prometheus" }
end

def get_alertmanager_service_type():
  if data.values.monitoring.alertmanager.service.type:
    return data.values.monitoring.alertmanager.service.type
  else:
    return get_service_type()
  end
end

def get_alertmanager_service_annotations():
  annotations = {}
  if globals.infrastructure_provider == "aws":
    annotations["service.beta.kubernetes.io/aws-load-balancer-backend-protocol"] = "tcp"
  end
  return annotations
end

def validate_alertmanager():
  validate_funcs = [validate_monitoring_namespace,
                    validate_alertmanager_image,
                    validate_alertmanager_deployment,
                    validate_alertmanager_rbac_component_names]
  for validate_func in validate_funcs:
    validate_func()
  end
end

def validate_alertmanager_image():
  # Image Name and version checking
  data.values.monitoring.alertmanager.image.name or assert.fail("missing alertmanager.image.name")
  data.values.monitoring.alertmanager.image.tag or assert.fail("missing alertmanager.image.tag")
  data.values.monitoring.alertmanager.image.repository or assert.fail("missing alertmanager.image.repository ")
  data.values.monitoring.alertmanager.image.pullPolicy or assert.fail("missing alertmanager.image.pullPolicy")
end

def validate_alertmanager_rbac_component_names():
  data.values.monitoring.alertmanager.service_account_name or assert.fail("missing alertmanager.service_account_name")
  data.values.monitoring.alertmanager.cluster_role_name or assert.fail("missing alertmanager.cluster_role_name")
end

def validate_alertmanager_deployment():
  data.values.monitoring.alertmanager.deployment.replicas or assert.fail("Alertmanager deployment replicas should be provided")
end

def labels_alertmanager():
  return { "component": "alertmanager", "app": "prometheus" }
end

def validate_kube_state_metrics():
  validate_funcs = [validate_monitoring_namespace,
                    validate_kube_state_metrics_image,
                    validate_kube_state_metrics_deployment,
                    validate_ksm_rbac_component_names]
  for validate_func in validate_funcs:
    validate_func()
  end
end

def validate_kube_state_metrics_image():
  # Image Name and version checking
  data.values.monitoring.kube_state_metrics.image.name or assert.fail("missing kube_state_metrics.image.name")
  data.values.monitoring.kube_state_metrics.image.tag or assert.fail("missing kube_state_metrics.image.tag")
  data.values.monitoring.kube_state_metrics.image.repository or assert.fail("missing kube_state_metrics.image.repository ")
  data.values.monitoring.kube_state_metrics.image.pullPolicy or assert.fail("missing kube_state_metrics.image.pullPolicy")
end

def validate_ksm_rbac_component_names():
  data.values.monitoring.kube_state_metrics.service_account_name or assert.fail("missing kube_state_metrics.service_account_name")
  data.values.monitoring.kube_state_metrics.cluster_role_name or assert.fail("missing kube_state_metrics.cluster_role_name")
end

def validate_kube_state_metrics_deployment():
  data.values.monitoring.kube_state_metrics.deployment.replicas or assert.fail("Kube State Metrics deployment replicas should be provided")
end

def labels_kube_state_metrics():
  return { "component": "kube-state-metrics", "app": "prometheus" }
end

def annotations():
  return { "prometheus.io/scrape": "true", "prometheus.io/port": "2020","prometheus.io/path": "/api/v1/metrics/prometheus" }
end

def validate_node_exporter():
  validate_funcs = [validate_monitoring_namespace,
                    validate_node_exporter_image,
                    validate_ne_rbac_component_names]
  for validate_func in validate_funcs:
    validate_func()
  end
end

def validate_node_exporter_image():
  # Image Name and version checking
  data.values.monitoring.node_exporter.image.name or assert.fail("missing node_exporter.image.name")
  data.values.monitoring.node_exporter.image.tag or assert.fail("missing node_exporter.image.tag")
  data.values.monitoring.node_exporter.image.repository or assert.fail("missing node_exporter.image.repository ")
  data.values.monitoring.node_exporter.image.pullPolicy or assert.fail("missing node_exporter.image.pullPolicy")
end

def validate_ne_rbac_component_names():
  data.values.monitoring.node_exporter.service_account_name or assert.fail("missing node_exporter.service_account_name")
  data.values.monitoring.node_exporter.cluster_role_name or assert.fail("missing node_exporter.cluster_role_name")
end

def labels_node_exporter():
  return { "component": "node-exporter", "app": "prometheus" }
end

def validate_pushgateway():
  validate_funcs = [validate_infrastructure_provider,
                    validate_monitoring_namespace,
                    validate_pushgateway_image,
                    validate_pushgateway_deployment,
                    validate_pushgateway_rbac_component_names]
  for validate_func in validate_funcs:
    validate_func()
  end
end

def validate_pushgateway_image():
  # Image Name and version checking
  data.values.monitoring.pushgateway.image.name or assert.fail("missing pushgateway.image.name")
  data.values.monitoring.pushgateway.image.tag or assert.fail("missing pushgateway.image.tag")
  data.values.monitoring.pushgateway.image.repository or assert.fail("missing pushgateway.image.repository ")
  data.values.monitoring.pushgateway.image.pullPolicy or assert.fail("missing pushgateway.image.pullPolicy")
end

def validate_pushgateway_rbac_component_names():
  data.values.monitoring.pushgateway.service_account_name or assert.fail("missing pushgateway.service_account_name")
  data.values.monitoring.pushgateway.cluster_role_name or assert.fail("missing pushgateway.cluster_role_name")
end

def validate_pushgateway_deployment():
  data.values.monitoring.pushgateway.deployment.replicas or assert.fail("Pushgateway deployment replicas should be provided")
end

def labels_pushgateway():
  return { "component": "pushgateway", "app": "prometheus" }
end

def validate_cadvisor():
  validate_funcs = [validate_monitoring_namespace,
                    validate_cadvisor_image,
                    validate_cadvisor_rbac_component_names]
  for validate_func in validate_funcs:
    validate_func()
  end
end

def validate_cadvisor_image():
  # Image Name and version checking
  data.values.monitoring.cadvisor.image.name or assert.fail("missing cadvisor.image.name")
  data.values.monitoring.cadvisor.image.tag or assert.fail("missing cadvisor.image.tag")
  data.values.monitoring.cadvisor.image.repository or assert.fail("missing cadvisor.image.repository ")
  data.values.monitoring.cadvisor.image.pullPolicy or assert.fail("missing cadvisor.image.pullPolicy")
end

def validate_cadvisor_rbac_component_names():
  data.values.monitoring.cadvisor.service_account_name or assert.fail("missing cadvisor.service_account_name")
  data.values.monitoring.cadvisor.cluster_role_name or assert.fail("missing cadvisor.cluster_role_name")
end

def labels_cadvisor():
  return { "component": "cadvisor", "app": "prometheus" }
end

#export
values = data.values

# validate monitoring components data values

validate_prometheus_server()
validate_alertmanager()
validate_kube_state_metrics()
validate_node_exporter()
validate_pushgateway()
validate_cadvisor()
