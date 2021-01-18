# Grafana Extension

## Prerequisites

* Workload cluster deployed.
* ytt installed (<https://github.com/k14s/ytt/releases>)
* kapp installed (<https://github.com/k14s/kapp/releases>)

### Deploy grafana extension

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

4. Create grafana namespace

    ```sh
    kubectl apply -f namespace-role.yaml
    ```

5. Copy `<INFRA_PROVIDER>/grafana-data-values.yaml.example` to `<INFRA_PROVIDER>/grafana-data-values.yaml`

   Configure grafana data values in `<INFRA_PROVIDER>/grafana-data-values.yaml`

   Supported configurations are documented in [grafana-configurations](../../../monitoring/grafana/README.md)

    Vsphere:

    ```sh
    cp vsphere/grafana-data-values.yaml.example vsphere/grafana-data-values.yaml
    ```

    Aws:

    ```sh
    cp aws/grafana-data-values.yaml.example aws/grafana-data-values.yaml
    ```

6. Create a secret with data values

    Vsphere:

    ```sh
    kubectl create secret generic grafana-data-values --from-file=values.yaml=vsphere/grafana-data-values.yaml -n tanzu-system-monitoring
    ```

    Aws:

    ```sh
    kubectl create secret generic grafana-data-values --from-file=values.yaml=aws/grafana-data-values.yaml -n tanzu-system-monitoring
    ```

7. Deploy grafana extension

    ```sh
    kubectl apply -f grafana-extension.yaml
    ```

8. Retrieve status of an extension

    ```sh
    kubectl get extension grafana -n tanzu-system-monitoring
    kubectl get app grafana -n tanzu-system-monitoring
    ```

   Grafana app status should change to `Reconcile Succeeded` once grafana is deployed successfully

   View detailed status

    ```sh
    kubectl get app grafana -n tanzu-system-monitoring -o yaml
    ```

### Update grafana extension

1. Get grafana data values from secret

    ```sh
    kubectl get secret grafana-data-values -n tanzu-system-monitoring -o 'go-template={{ index .data "values.yaml" }}' | base64 -d > grafana-data-values.yaml
    ```

2. Update grafana data values in grafana-data-values.yaml

3. Update grafana data values secret

    ```sh
    kubectl create secret generic grafana-data-values --from-file=values.yaml=grafana-data-values.yaml -n tanzu-system-monitoring -o yaml --dry-run | kubectl replace -f-
    ```

   Grafana extension will be reconciled again with the above data values

   **NOTE:**
   By default, kapp-controller will sync apps every 5 minutes. So, the update should take effect in <= 5 minutes.
   If you want the update to take effect immediately, change syncPeriod in `grafana-extension.yaml` to a lesser value
   and apply grafana extension `kubectl apply -f grafana-extension.yaml`.

4. Refer to `Retrieve status of an extension` in [deploy grafana extension](#deploy-grafana-extension) to retrieve the status of an extension

### Delete grafana extension

1. Delete grafana extension

    ```sh
    kubectl delete -f grafana-extension.yaml
    kubectl delete app grafana -n tanzu-system-monitoring
    ```

2. Refer to `Retrieve status of an extension` in [deploy grafana extension](#deploy-grafana-extension) to retrieve the status of an extension

   If extension is deleted successfully, then get of both grafana extension and app should return `Not Found`

3. Delete grafana namespace

   **NOTE: Do not delete namespace-role.yaml before app is deleted fully, as it will lead to errors due to service account used by kapp-controller being deleted**

    ```sh
    kubectl delete -f namespace-role.yaml
    ```

### Upgrade grafana deployment to grafana extension

1. Get grafana configmap

    ```sh
    kubectl get configmap grafana -n tanzu-system-monitoring -o 'go-template={{ index .data "grafana.yaml" }}' > grafana-configmap.yaml
    ```

2. Delete existing grafana deployment

    ```sh
    kubectl delete namespace tanzu-system-monitoring
    ```

3. Follow steps in [Deploy grafana extension](#deploy-grafana-extension) to deploy grafana extension

### Test template rendering

1. Test if grafana templates are rendered correctly

    ```sh
    ytt --ignore-unknown-comments -f ../../../common -f ../../../monitoring/grafana -f grafana-data-values.yaml
    ```
