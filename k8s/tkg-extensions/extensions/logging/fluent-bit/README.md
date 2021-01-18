# Fluent Bit Extension

## Prerequisites

* Workload cluster deployed.
* ytt installed (<https://github.com/k14s/ytt/releases>)
* kapp installed (<https://github.com/k14s/kapp/releases>)

### Deploy fluent-bit extension

1. Install TMC's extension manager

    ```sh
    kubectl apply -f ../../tmc-extension-manager.yaml
    ```

2. Install kapp-controller

    ```sh
    kubectl apply -f ../../kapp-controller.yaml
    ```

3. Create fluent-bit namespace

    ```sh
    kubectl apply -f namespace-role.yaml
    ```

4. Copy `<LOG_BACKEND>/fluent-bit-data-values.yaml.example` to `<LOG_BACKEND>/fluent-bit-data-values.yaml`

   Configure fluent-bit data values in `<LOG_BACKEND>/fluent-bit-data-values.yaml`

   Supported configurations are documented in [fluent-bit-configurations](../../../logging/fluent-bit/README.md)

   Copy the data values files for your log backend

    ```sh
    cp elasticsearch/fluent-bit-data-values.yaml.example elasticsearch/fluent-bit-data-values.yaml
    cp kafka/fluent-bit-data-values.yaml.example kafka/fluent-bit-data-values.yaml
    cp splunk/fluent-bit-data-values.yaml.example splunk/fluent-bit-data-values.yaml
    cp http/fluent-bit-data-values.yaml.example http/fluent-bit-data-values.yaml
    ```

5. Create a secret with data values

   Create secret for your log backend

   ```sh
   kubectl create secret generic fluent-bit-data-values --from-file=values.yaml=elasticsearch/fluent-bit-data-values.yaml -n tanzu-system-logging
   kubectl create secret generic fluent-bit-data-values --from-file=values.yaml=kafka/fluent-bit-data-values.yaml -n tanzu-system-logging
   kubectl create secret generic fluent-bit-data-values --from-file=values.yaml=splunk/fluent-bit-data-values.yaml -n tanzu-system-logging
   kubectl create secret generic fluent-bit-data-values --from-file=values.yaml=http/fluent-bit-data-values.yaml -n tanzu-system-logging
   ```
  
6. Deploy fluent-bit extension

    ```sh
    kubectl apply -f fluent-bit-extension.yaml
   ```

7. Retrieve status of an extension

    ```sh
    kubectl get extension fluent-bit -n tanzu-system-logging
    kubectl get app fluent-bit -n tanzu-system-logging
    ```

   Fluent Bit app status should change to `Reconcile Succeeded` once fluent-bit is deployed successfully

   View detailed status

   ```sh
   kubectl get app fluent-bit -n tanzu-system-logging -o yaml
   ```

### Update fluent-bit extension

1. Get fluent-bit data values from secret

    ```sh
    kubectl get secret fluent-bit-data-values -n tanzu-system-logging -o 'go-template={{ index .data "values.yaml" }}' | base64 -d > fluent-bit-data-values.yaml
    ```

2. Update fluent-bit data values in fluent-bit-data-values.yaml

3. Update fluent-bit data values secret

    ```sh
    kubectl create secret generic fluent-bit-data-values --from-file=values.yaml=fluent-bit-data-values.yaml -n tanzu-system-logging -o yaml --dry-run | kubectl replace -f-
    ```

   Fluent Bit extension will be reconciled again with the above data values

   **NOTE:**
   By default, kapp-controller will sync apps every 5 minutes. So, the update should take effect in <= 5 minutes.
   If you want the update to take effect immediately, change syncPeriod in `fluent-bit-extension.yaml` to a lesser value
   and apply fluent-bit extension `kubectl apply -f fluent-bit-extension.yaml`.

4. Refer to `Retrieve status of an extension` in [deploy fluent-bit extension](#deploy-fluent-bit-extension) to retrieve the status of an extension

### Delete fluent-bit extension

1. Delete fluent-bit extension

    ```sh
    kubectl delete -f fluent-bit-extension.yaml
    kubectl delete app fluent-bit -n tanzu-system-logging
    ```

2. Refer to `Retrieve status of an extension` in [deploy fluent-bit extension](#deploy-fluent-bit-extension) to retrieve the status of an extension

   If extension is deleted successfully, then get of both fluent-bit extension and app should return `Not Found`

3. Delete fluent-bit namespace

   **NOTE: Do not delete namespace-role.yaml before app is deleted fully, as it will lead to errors due to service account used by kapp-controller being deleted**

    ```sh
    kubectl delete -f namespace-role.yaml
    ```

### Upgrade fluent-bit deployment to fluent-bit extension

1. Get fluent-bit configmap

    ```sh
    kubectl get configmap fluent-bit -n tanzu-system-logging -o 'go-template={{ index .data "fluent-bit.yaml" }}' > fluent-bit-configmap.yaml
    ```

2. Delete existing fluent-bit deployment

    ```sh
    kubectl delete namespace tanzu-system-logging
    ```

3. Follow steps in [Deploy fluent-bit extension](#deploy-fluent-bit-extension) to deploy fluent-bit extension

### Test template rendering

1. Test if fluent-bit templates are rendered correctly

    ```sh
    ytt --ignore-unknown-comments -f ../../../common -f ../../../logging/fluent-bit -f fluent-bit-data-values.yaml
    ```
