load("@ytt:data", "data")
load("globals.star", "globals")
load("@ytt:assert", "assert")
load("/globals.star", "validate_infrastructure_provider")

SERVICE_TYPE_NODEPORT = "NodePort"
SERVICE_TYPE_LOADBALANCER = "LoadBalancer"

def validate_gangway():
  validate_funcs = [validate_infrastructure_provider,
                    validate_gangway_namespace,
                    validate_gangway_config,
                    validate_gangway_image,
                    validate_gangway_certificate,
                    validate_gangway_deployment,
                    validate_gangway_service,
                    validate_gangway_secret,
                    validate_dex_cert]
  for validate_func in validate_funcs:
    validate_func()
  end
end

def validate_gangway_namespace():
  data.values.gangway.namespace or assert.fail("Gangway namespace should be provided")
end

def validate_gangway_config():
  data.values.gangway.config.clusterName or assert.fail("Gangway clusterName <WORKLOAD_CLUSTER_NAME> should be provided")
  data.values.gangway.config.clientID or assert.fail("Gangway clientID <WORKLOAD_CLUSTER_NAME> should be provided")
  if globals.infrastructure_provider == "aws" :
    data.values.gangway.config.DEX_SVC_LB_HOSTNAME or assert.fail("Gangway DEX_SVC_LB_HOSTNAME should be provided")
  elif globals.infrastructure_provider == "azure" :
    data.values.gangway.config.DEX_SVC_LB_HOSTNAME or assert.fail("Gangway DEX_SVC_LB_HOSTNAME should be provided")
  else:
    data.values.gangway.config.MGMT_CLUSTER_IP or assert.fail("Gangway MGMT_CLUSTER_IP should be provided")
  end
  data.values.gangway.config.APISERVER_URL or assert.fail("Gangway APISERVER_URL should be provided")
  data.values.gangway.config.apiPort or assert.fail("Gangway apiPort should be provided")

  if globals.infrastructure_provider == "vsphere":
    data.values.gangway.config.authPort and data.values.gangway.config.redirectPort and data.values.dns.vsphere.ipAddresses or assert.fail("Gangway WORKLOAD_CLUSTER_IP and authPort should be provided")
  end
  data.values.gangway.config.serveTLS or assert.fail("gangway serveTLS true by default should be provided")
  data.values.gangway.config.certFile or assert.fail("gangway certFile /tls/tls.crt by default should be provided")
  data.values.gangway.config.keyFile or assert.fail("gangway keyFile /tls/tls.key by default should be provided")
  data.values.gangway.config.trustedCAPath or assert.fail("gangway trustedCAPath /etc/dex/dex-ca.crt by default should be provided")
  data.values.gangway.config.idpCAPath or assert.fail("gangway idpCAPath /etc/dex/dex-ca.crt by default should be provided")
  data.values.gangway.config.scopes or assert.fail("gangway scopes should be provided")

end

def validate_gangway_image():
  data.values.gangway.image.name or assert.fail("gangway image name should be provided")
  data.values.gangway.image.tag or assert.fail("gangway image tag should be provided")
  data.values.gangway.image.repository or assert.fail("gangway image repository should be provided")
  data.values.gangway.image.pullPolicy or assert.fail("gangway image pullPolicy should be provided")
end

def validate_gangway_certificate():
  data.values.gangway.certificate.duration or assert.fail("gangway certificate duration should be provided")
  data.values.gangway.certificate.renewBefore or assert.fail("gangway certificate renewBefore should be provided")
end

def validate_gangway_deployment():
  data.values.gangway.deployment.replicas or assert.fail("gangway deployment replicas should be provided")
end

def validate_gangway_service():
  if data.values.gangway.service.type:
    data.values.gangway.service.type in ("LoadBalancer", "NodePort") or assert.fail("Gangway service type should be LoadBalancer or NodePort")
  end
  if globals.infrastructure_provider == "aws":
    data.values.dns.aws.GANGWAY_SVC_LB_HOSTNAME or assert.fail("Gangway aws dnsname GANGWAY_SVC_LB_HOSTNAME should be provided")
  end
  if globals.infrastructure_provider == "vsphere":
    data.values.dns.vsphere.ipAddresses[0] or assert.fail("gangway vsphere dns at least one ipaddress should be provided")
  end
end

def validate_gangway_secret():
  data.values.gangway.secret.sessionKey or assert.fail("gangway sessionKey should be provided")
  data.values.gangway.secret.clientSecret or assert.fail("gangway clientSecret should be provided")
end

def validate_dex_cert():
  data.values.dex.ca or assert.fail("gangway needs ca cert of dex running in management cluster")
end

def get_service_type():
  if globals.infrastructure_provider == "vsphere":
    return SERVICE_TYPE_NODEPORT
  else:
    return SERVICE_TYPE_LOADBALANCER
  end
end

def get_gangway_service_type():
  if hasattr(data.values.gangway, "service") and hasattr(data.values.gangway.service, "type") and data.values.gangway.service.type != None:
    return data.values.gangway.service.type
  else:
    return get_service_type()
  end
end

def is_service_type_LB():
  return get_gangway_service_type() == SERVICE_TYPE_LOADBALANCER
end

def is_service_NodePort():
  return get_gangway_service_type() == SERVICE_TYPE_NODEPORT
end

def get_gangway_service_annotations():
  if globals.infrastructure_provider == "aws":
    return {"service.beta.kubernetes.io/aws-load-balancer-backend-protocol": "ssl"}
  else:
    return {}
  end
end

#export
values = data.values

# validate gangway
validate_gangway()
