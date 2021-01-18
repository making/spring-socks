load("@ytt:data", "data")
load("@ytt:assert", "assert")
load("/globals.star", "globals", "get_kapp_disable_wait_annotations")

SERVICE_TYPE_NODEPORT = "NodePort"
SERVICE_TYPE_LOADBALANCER = "LoadBalancer"

def validate_contour():
  validate_funcs = [validate_infrastructure_provider,
                    validate_contour_namespace,
                    validate_contour_config,
                    validate_contour_deployment,
                    validate_contour_certificate,
                    validate_contour_image,
                    validate_envoy_image,
                    validate_envoy_deployment,
                    validate_envoy_service]
   for validate_func in validate_funcs:
     validate_func()
   end
end

def validate_infrastructure_provider():
  data.values.infrastructure_provider in ("aws", "vsphere", "azure") or assert.fail("infrastructure provider should be either aws or vsphere or azure")
end

def validate_contour_namespace():
  data.values.contour.namespace or assert.fail("Contour namespace should be provided")
end

def validate_contour_config():
  data.values.contour.config.accesslogFormat in ("envoy", "json") or assert.fail("Contour config accesslogFormat should be envoy or json")
  data.values.contour.config.leaderelection.configmapName or assert.fail("Contour config leader election configmap name should be provided")
  data.values.contour.config.leaderelection.configmapNamespace or assert.fail("Contour config leader election configmap namespace should be provided")
end

def validate_contour_deployment():
  data.values.contour.deployment.replicas or assert.fail("Contour deployment replicas should be provided")
end

def validate_contour_certificate():
  data.values.contour.certificate.duration or assert.fail("Contour certificate duration should be provided")
  data.values.contour.certificate.renewBefore or assert.fail("Contour certificate renewBefore should be provided")
end

def validate_contour_image():
  data.values.contour.image.name or assert.fail("Contour image name should be provided")
  data.values.contour.image.tag or assert.fail("Contour image tag should be provided")
  data.values.contour.image.repository or assert.fail("Contour image repository should be provided")
  data.values.contour.image.pullPolicy or assert.fail("Contour image pullPolicy should be provided")
end

def validate_envoy_image():
  data.values.envoy.image.name or assert.fail("Envoy image name should be provided")
  data.values.envoy.image.tag or assert.fail("Envoy image tag should be provided")
  data.values.envoy.image.repository or assert.fail("Envoy image repository should be provided")
  data.values.envoy.image.pullPolicy or assert.fail("Envoy image pullPolicy should be provided")
end

def validate_envoy_deployment():
  if data.values.envoy.hostPort.enable:
    data.values.envoy.hostPort.http or assert.fail("Envoy http hostPort should be provided if enableHostPort is true")
    data.values.envoy.hostPort.https or assert.fail("Envoy https hostPort should be provided if enableHostPort is true")
  end
  data.values.envoy.loglevel or assert.fail("Envoy log level should be provided")
  data.values.envoy.loglevel in ("trace", "debug", "info", "warning", "warn", "error", "critical", "off") or assert.fail("Envoy log level should be trace|debug|info|warning/warn|error|critical|off")
  data.values.envoy.deployment.terminationGracePeriodSeconds or assert.fail("Envoy deployment terminationGracePeriodSeconds should be provided")
end

def validate_envoy_service():
  if data.values.envoy.service.type:
    data.values.envoy.service.type in ("LoadBalancer", "NodePort", "ClusterIP") or assert.fail("Envoy service type should be LoadBalancer or NodePort or ClusterIP")
  end
  if data.values.envoy.service.externalTrafficPolicy:
    data.values.envoy.service.externalTrafficPolicy in ("Cluster" or "Local") or assert.fail("Envoy service externalTrafficPolicy should be Cluster or Local")
  end
  if globals.infrastructure_provider == "aws":
    data.values.envoy.service.aws.LBType in ("classic", "nlb") or assert.fail("Envoy service aws LB Type should be classic or nlb")
  end
end

def get_service_type():
  if globals.infrastructure_provider == "vsphere":
    return SERVICE_TYPE_NODEPORT
  else:
    return SERVICE_TYPE_LOADBALANCER
  end
end

def get_envoy_service_type():
  if data.values.envoy.service.type:
    return data.values.envoy.service.type
  else:
    return get_service_type()
  end
end

def is_service_type_LB():
  return get_envoy_service_type() == SERVICE_TYPE_LOADBALANCER
end

def get_envoy_service_annotations():
  annotations = {}
  # This annotation tells kapp to disable wait for getting service type loadbalancer public IP.
  # This annotation is expected to be only used for local kind testing where service type loadbalancer will not get a public IP.
  if data.values.envoy.service.disableWait:
    annotations.update(get_kapp_disable_wait_annotations())
  end
  if globals.infrastructure_provider == "aws":
    if data.values.envoy.service.aws.LBType == "nlb":
      annotations["service.beta.kubernetes.io/aws-load-balancer-type"] = "nlb"
    else:
      annotations["service.beta.kubernetes.io/aws-load-balancer-backend-protocol"] = "tcp"
      if data.values.contour.config.useProxyProtocol:
        annotations["service.beta.kubernetes.io/aws-load-balancer-proxy-protocol"] = "*"
      end
    end
  end
  return annotations
end

#export
values = data.values

# validate contour and envoy data values
validate_contour()
