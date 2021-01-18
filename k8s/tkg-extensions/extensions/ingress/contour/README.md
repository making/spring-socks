# Contour Extension

## Prerequisites

* Workload cluster deployed.
* ytt installed (<https://github.com/k14s/ytt/releases>)
* kapp installed (<https://github.com/k14s/kapp/releases>)

### Deploy contour extension

1. Install TMC's extension manager

    ```sh
    kubectl apply -f ../../tmc-extension-manager.yaml
    ```

2. Install kapp-controller

    ```sh
    kubectl apply -f ../../kapp-controller.yaml
    ```

3. Deploy cert-manager if its not already installed

    ```sh
    kubectl apply -f ../../../cert-manager/
    ```

4. Create contour namespace

    ```sh
    kubectl apply -f namespace-role.yaml
    ```

5. Copy `<INFRA_PROVIDER>/contour-data-values.yaml.example` to `<INFRA_PROVIDER>/contour-data-values.yaml`

   Configure contour data values in `<INFRA_PROVIDER>/contour-data-values.yaml`

   Supported configurations are documented in [contour-configurations](../../../ingress/contour/README.md)

    Vsphere:

    ```sh
    cp vsphere/contour-data-values.yaml.example vsphere/contour-data-values.yaml
    ```

    Aws:

    ```sh
    cp aws/contour-data-values.yaml.example aws/contour-data-values.yaml
    ```

6. Create a secret with data values

    Vsphere:

    ```sh
    kubectl create secret generic contour-data-values --from-file=values.yaml=vsphere/contour-data-values.yaml -n tanzu-system-ingress
    ```

    Aws:

    ```sh
    kubectl create secret generic contour-data-values --from-file=values.yaml=aws/contour-data-values.yaml -n tanzu-system-ingress
    ```

7. Deploy contour extension

    ```sh
    kubectl apply -f contour-extension.yaml
   ```

8. Retrieve status of an extension

    ```sh
    kubectl get extension contour -n tanzu-system-ingress
    kubectl get app contour -n tanzu-system-ingress
    ```

   Contour app status should change to `Reconcile Succeeded` once contour is deployed successfully

   View detailed status

   ```sh
   kubectl get app contour -n tanzu-system-ingress -o yaml
   ```

### Update contour extension

1. Get contour data values from secret

    ```sh
    kubectl get secret contour-data-values -n tanzu-system-ingress -o 'go-template={{ index .data "values.yaml" }}' | base64 -d > contour-data-values.yaml
    ```

2. Update contour data values in contour-data-values.yaml

3. Update contour data values secret

    ```sh
    kubectl create secret generic contour-data-values --from-file=values.yaml=contour-data-values.yaml -n tanzu-system-ingress -o yaml --dry-run | kubectl replace -f-
    ```

   Contour extension will be reconciled again with the above data values

   **NOTE:**
   By default, kapp-controller will sync apps every 5 minutes. So, the update should take effect in <= 5 minutes.
   If you want the update to take effect immediately, change syncPeriod in `contour-extension.yaml` to a lesser value
   and apply contour extension `kubectl apply -f contour-extension.yaml`.

4. Refer to `Retrieve status of an extension` in [deploy contour extension](#deploy-contour-extension) to retrieve the status of an extension

### Delete contour extension

1. Delete contour extension

    ```sh
    kubectl delete -f contour-extension.yaml
    kubectl delete app contour -n tanzu-system-ingress
    ```

2. Refer to `Retrieve status of an extension` in [deploy contour extension](#deploy-contour-extension) to retrieve the status of an extension

   If extension is deleted successfully, then get of contour app should return `Not Found`

3. Delete contour namespace

   **NOTE: Do not delete namespace-role.yaml before app is deleted fully, as it will lead to errors due to service account used by kapp-controller being deleted**

    ```sh
    kubectl delete -f namespace-role.yaml
    ```

### Upgrade contour deployment to contour extension

1. Get contour configmap

    ```sh
    kubectl get configmap contour -n tanzu-system-ingress -o 'go-template={{ index .data "contour.yaml" }}' > contour-configmap.yaml
    ```

2. Delete existing contour deployment

    ```sh
    kubectl delete namespace tanzu-system-ingress
    ```

3. Follow steps in [Deploy contour extension](#deploy-contour-extension) to deploy contour extension

### Test template rendering

1. Test if contour templates are rendered correctly

    ```sh
    ytt --ignore-unknown-comments -f ../../../common -f ../../../ingress/contour -f contour-data-values.yaml
    ```
