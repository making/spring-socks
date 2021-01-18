# Harbor Extension

## Prerequisites

* Workload Cluster deployed.
* Contour Extension installed in the Workload Cluster.

### Install Harbor Extension

1. Install TMC Extension Manager

    ```sh
    kubectl apply -f ../../tmc-extension-manager.yaml
    ```

2. Install kapp-controller

    ```sh
    kubectl apply -f ../../kapp-controller.yaml
    ```

3. Create namespace and roles for Harbor extension

    ```sh
    kubectl apply -f namespace-role.yaml
    ```

4. Copy `harbor-data-values.yaml.example` to `harbor-data-values.yaml`

    ```sh
    cp harbor-data-values.yaml.example harbor-data-values.yaml
    ```

   Specify the mandatory passwords and secrets in `harbor-data-values.yaml`, or run `bash generate-passwords.sh harbor-data-values.yaml` to generate them automatically. This step is needed only once.

   Specify other Harbor configuration (e.g. admin password, hostname, persistence setting, etc.) in `harbor-data-values.yaml`.

   **NOTE**: If the default storageClass in the Workload Cluster, or the specified storageClass in `harbor-data-values.yaml` supports the accessMode [ReadWriteMany](https://kubernetes.io/docs/concepts/storage/persistent-volumes/#access-modes), make sure to update the accessMode to ReadWriteMany in `harbor-data-values.yaml`. [vSphere 7.0 supports the accessMode ReadWriteMany](https://blogs.vmware.com/virtualblocks/2020/03/12/cloud-native-storage-and-vsan-file-services-integration/) whereas vSphere 6.7U3 doesn't support it.

5. Create a secret with data values

    ```sh
    kubectl create secret generic harbor-data-values --from-file=values.yaml=harbor-data-values.yaml -n tanzu-system-registry
    ```

6. Deploy Harbor Extension

    ```sh
    kubectl apply -f harbor-extension.yaml
    ```

7. Retrieve the status of Harbor Extension

    ```sh
    kubectl get extension harbor -n tanzu-system-registry
    kubectl get app harbor -n tanzu-system-registry
    ```

   The Harbor App status should change to `Reconcile succeeded` once Harbor is deployed successfully.

   View detailed status:

   ```sh
   kubectl get app harbor -n tanzu-system-registry -o yaml
   ```

### Update Harbor Extension

1. Use the previous applied harbor-data-values.yaml or get Harbor data values from the secret

    ```sh
    kubectl get secret harbor-data-values -n tanzu-system-registry -o 'go-template={{ index .data "values.yaml" }}' | base64 -d > harbor-data-values.yaml
    ```

2. Update Harbor configuration in harbor-data-values.yaml

3. Update harbor-data-values secret

    ```sh
    kubectl create secret generic harbor-data-values --from-file=values.yaml=harbor-data-values.yaml -n tanzu-system-registry -o yaml --dry-run | kubectl replace -f-
    ```

   Harbor extension will be reconciled again with the above data values.

   **NOTE:**
   By default, kapp-controller will sync apps every 5 minutes. So, the update should take effect in <= 5 minutes.
   If you want the update to take effect immediately, change syncPeriod in `harbor-extension.yaml` to a lesser value
   and apply harbor extension `kubectl apply -f harbor-extension.yaml`.

4. Retrieve the status of Harbor Extension

   Refer to `Retrieve the status of Harbor Extension` in section [Install Harbor extension](#install-harbor-extension).

### Uninstall Harbor Extension

1. Delete Harbor Extension

    ```sh
    kubectl delete -f harbor-extension.yaml
    kubectl delete app harbor -n tanzu-system-registry
    ```

2. Retrieve the status of Harbor Extension

   Refer to `Retrieve the status of Harbor Extension` in section [Install Harbor extension](#install-harbor-extension). If Harbor Extension is deleted successfully, then getting Harbor App will show `not found`.

3. Delete Harbor namespace

   **NOTE: Do not delete namespace-role.yaml before app is deleted fully, as it will lead to errors due to service account used by kapp-controller being deleted**

    ```sh
    kubectl delete -f namespace-role.yaml
    ```

### Test template rendering

1. Test if Harbor ytt templates are rendered correctly

    ```sh
    ytt --ignore-unknown-comments -f ../../../common -f ../../../registry/harbor -f harbor-data-values.yaml
    ```
