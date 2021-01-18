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
  return get_grafana_service_type() == SERVICE_TYPE_LOADBALANCER
end

def get_grafana_service_type():
  if data.values.monitoring.grafana.service.type:
    return data.values.monitoring.grafana.service.type
  else:
    return get_service_type()
  end
end

def validate_monitoring_namespace():
  # Namespace checking
  data.values.monitoring.namespace or assert.fail("missing monitoring.namespace")
end

def validate_grafana():
  validate_funcs = [validate_infrastructure_provider,
                    validate_monitoring_namespace,
                    validate_grafana_password,
                    validate_grafana_deployment,
                    validate_grafana_image,
                    validate_grafana_rbac_component_names]
  for validate_func in validate_funcs:
    validate_func()
  end
end

def validate_grafana_deployment():
  data.values.monitoring.grafana.deployment.replicas or assert.fail("Grafana deployment replicas should be provided")
end

def validate_grafana_image():
  # Image Name and version checking
  data.values.monitoring.grafana.image.name or assert.fail("missing grafana.image.name")
  data.values.monitoring.grafana.image.tag or assert.fail("missing grafana.image.tag")
  data.values.monitoring.grafana.image.repository or assert.fail("missing grafana.image.repository ")
  data.values.monitoring.grafana.image.pullPolicy or assert.fail("missing grafana.image.pullPolicy")
end

def validate_grafana_rbac_component_names():
  data.values.monitoring.grafana.service_account_name or assert.fail("missing grafana.service_account_name")
  data.values.monitoring.grafana.cluster_role_name or assert.fail("missing grafana.cluster_role_name")
end

def validate_grafana_password():
  # Grafana password checking
  data.values.monitoring.grafana.secret.admin_password or assert.fail("missing grafana password")
end

#export
values = data.values

# validate monitoring components data values
validate_grafana()
