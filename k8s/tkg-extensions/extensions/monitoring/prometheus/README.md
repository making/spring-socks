# Prometheus Extension

## Prerequisites

* Workload cluster deployed.
* ytt installed (<https://github.com/k14s/ytt/releases>)
* kapp installed (<https://github.com/k14s/kapp/releases>)

### Deploy prometheus extension

1. Install TMC's extension manager

    ```sh
    kubectl apply -f tmc-extension-manager.yaml
    ```

2. Install kapp-controller

    ```sh
    kubectl apply -f kapp-controller.yaml
    ```

3. Deploy cert-manager if its not already installed

    ```sh
    kubectl apply -f ../../../cert-manager/
    ```

4. Create prometheus namespace

    ```sh
    kubectl apply -f namespace-role.yaml
    ```

5. Copy `<INFRA_PROVIDER>/prometheus-data-values.yaml.example` to `<INFRA_PROVIDER>/prometheus-data-values.yaml`

   Configure prometheus data values in `<INFRA_PROVIDER>/prometheus-data-values.yaml`

   Supported configurations are documented in [prometheus-configurations](../../../monitoring/prometheus/README.md)

    Vsphere:

    ```sh
    cp vsphere/prometheus-data-values.yaml.example vsphere/prometheus-data-values.yaml
    ```

    Aws:

    ```sh
    cp aws/prometheus-data-values.yaml.example aws/prometheus-data-values.yaml
    ```

6. Create a secret with data values

    Vsphere:

    ```sh
    kubectl create secret generic prometheus-data-values --from-file=values.yaml=vsphere/prometheus-data-values.yaml -n tanzu-system-monitoring
    ```

    Aws:

    ```sh
    kubectl create secret generic prometheus-data-values --from-file=values.yaml=aws/prometheus-data-values.yaml -n tanzu-system-monitoring
    ```

7. Deploy prometheus extension

    ```sh
    kubectl apply -f prometheus-extension.yaml
    ```

8. Retrieve status of an extension

    ```sh
    kubectl get extension prometheus -n tanzu-system-monitoring
    kubectl get app prometheus -n tanzu-system-monitoring
    ```

   Prometheus app status should change to `Reconcile Succeeded` once prometheus is deployed successfully

   View detailed status

    ```sh
    kubectl get app prometheus -n tanzu-system-monitoring -o yaml
    ```

### Update prometheus extension

1. Get prometheus data values from secret

    ```sh
    kubectl get secret prometheus-data-values -n tanzu-system-monitoring -o 'go-template={{ index .data "values.yaml" }}' | base64 -d > prometheus-data-values.yaml
    ```

2. Update prometheus data values in prometheus-data-values.yaml

3. Update prometheus data values secret

    ```sh
    kubectl create secret generic prometheus-data-values --from-file=values.yaml=prometheus-data-values.yaml -n tanzu-system-monitoring -o yaml --dry-run | kubectl replace -f-
    ```

   Prometheus extension will be reconciled again with the above data values

   **NOTE:**
   By default, kapp-controller will sync apps every 5 minutes. So, the update should take effect in <= 5 minutes.
   If you want the update to take effect immediately, change syncPeriod in `prometheus-extension.yaml` to a lesser value
   and apply prometheus extension `kubectl apply -f prometheus-extension.yaml`.

4. Refer to `Retrieve status of an extension` in [deploy prometheus extension](#deploy-prometheus-extension) to retrieve the status of an extension

### Delete prometheus extension

1. Delete prometheus extension

    ```sh
    kubectl delete -f prometheus-extension.yaml
    kubectl delete app prometheus -n tanzu-system-monitoring
    ```

2. Refer to `Retrieve status of an extension` in [deploy prometheus extension](#deploy-prometheus-extension) to retrieve the status of an extension

   If extension is deleted successfully, then get of both prometheus extension and app should return `Not Found`

3. Delete prometheus namespace

   **NOTE: Do not delete namespace-role.yaml before app is deleted fully, as it will lead to errors due to service account used by kapp-controller being deleted**

    ```sh
    kubectl delete -f namespace-role.yaml
    ```

### Upgrade prometheus deployment to prometheus extension

1. Get prometheus configmap

    ```sh
    kubectl get configmap prometheus -n tanzu-system-monitoring -o 'go-template={{ index .data "prometheus.yaml" }}' > prometheus-configmap.yaml
    ```

2. Delete existing prometheus deployment

    ```sh
    kubectl delete namespace tanzu-system-monitoring
    ```

3. Follow steps in [Deploy prometheus extension](#deploy-prometheus-extension) to deploy prometheus extension

### Test template rendering

1. Test if prometheus templates are rendered correctly

    ```sh
    ytt --ignore-unknown-comments -f ../../../common -f ../../../monitoring/prometheus -f prometheus-data-values.yaml
    ```
