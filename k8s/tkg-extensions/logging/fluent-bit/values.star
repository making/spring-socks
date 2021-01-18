load("@ytt:data", "data")
load("@ytt:assert", "assert")

def validate_fluent_bit():
  validate_funcs = [validate_fluent_bit_namespace,
                    validate_fluent_bit_image,
                    validate_fluent_bit_output_plugin,
                    validate_fluent_bit_data_values,
                    validate_rbac_component_names,
                    validate_tkg_data_values]
   for validate_func in validate_funcs:
     validate_func()
   end
end

def validate_fluent_bit_namespace():
  # Namespace checking
  data.values.logging.namespace or assert.fail("missing logging.namespace")
end

def validate_fluent_bit_output_plugin():
  # Checking for supported fluent bit backends
  data.values.fluent_bit.output_plugin in ("elasticsearch", "kafka", "splunk", "http") or assert.fail("fluent bit output plugin should be either elasticsearch or kafka or splunk or http")
end

def validate_fluent_bit_image():
  # Image Name and version checking
  data.values.logging.image.name or assert.fail("missing logging.image.name")
  data.values.logging.image.tag or assert.fail("missing logging.image.tag")
  data.values.logging.image.repository or assert.fail("missing logging.image.repository ")
  data.values.logging.image.pullPolicy or assert.fail("missing logging.image.pullPolicy")
end

def validate_rbac_component_names():
  data.values.logging.service_account_name or assert.fail("missing logging.service_account_name")
  data.values.logging.cluster_role_name or assert.fail("missing logging.cluster_role_name")
end

def validate_tkg_data_values():
  data.values.tkg.cluster_name or assert.fail("missing tkg.cluster_name ")
  data.values.tkg.instance_name or assert.fail("missing tkg.instance_name ")
end

def validate_fluent_bit_data_values():
  #Checking for params required for different backends for fluent bit
  if data.values.fluent_bit.output_plugin == "elasticsearch":
    data.values.fluent_bit.elasticsearch.host or assert.fail("missing elasticsearch.host ")
    data.values.fluent_bit.elasticsearch.port or assert.fail("missing elasticsearch.port ")
  elif data.values.fluent_bit.output_plugin == "kafka":
    data.values.fluent_bit.kafka.broker_service_name or assert.fail("missing kafka.broker_service_name ")
    data.values.fluent_bit.kafka.topic_name or assert.fail("missing kafka.topic_name ")
  elif data.values.fluent_bit.output_plugin == "splunk":
    data.values.fluent_bit.splunk.host or assert.fail("missing splunk.host ")
    data.values.fluent_bit.splunk.port or assert.fail("missing splunk.port ")
    data.values.fluent_bit.splunk.token or assert.fail("missing splunk.token ")
  elif data.values.fluent_bit.output_plugin == "http":
    data.values.fluent_bit.http.host or assert.fail("missing http.host ")
    data.values.fluent_bit.http.port or assert.fail("missing http.port ")
    data.values.fluent_bit.http.uri or assert.fail("missing http.uri ")
    data.values.fluent_bit.http.header_key_value or assert.fail("missing http.header_key_value ")
    data.values.fluent_bit.http.format or assert.fail("missing http.format ")
  end
end

def labels():
    return { "k8s-app": "fluent-bit-logging", "version": "v1", "kubernetes.io/cluster-service": "true" }
end

def annotations():
    return { "prometheus.io/scrape": "true", "prometheus.io/port": "2020","prometheus.io/path": "/api/v1/metrics/prometheus" }
end

#export
values = data.values

# validate fluent bit data values
validate_fluent_bit()