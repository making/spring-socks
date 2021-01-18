# Gangway Extension

## Prerequisites

* Workload cluster with ***OIDC*** plan deployed.
* ytt installed (<https://github.com/k14s/ytt/releases>)
* kapp installed (<https://github.com/k14s/kapp/releases>)

### Deploy gangway extension

#### Vsphere

1. Install TMC's extension manager

    ```sh
    kubectl apply -f tmc-extension-manager.yaml
    ```

2. Install kapp-controller

    ```sh
    kubectl apply -f kapp-controller.yaml
    ```

3. Deploy cert-manager extension

    ```sh
    kubectl apply -f ../../../cert-manager/
    ```

4. Create gangway namespace

    ```sh
    kubectl apply -f namespace-role.yaml
    ```

5. Copy `<INFRA_PROVIDER>/gangway-data-values.yaml.example` to `<INFRA_PROVIDER>/gangway-data-values.yaml`

   Configure gangway data values in `<INFRA_PROVIDER>/gangway-data-values.yaml`

   Supported configurations are documented in [gangway-configurations](../../../authentication/gangway/README.md)

    ```sh
    cp vsphere/gangway-data-values.yaml.example vsphere/gangway-data-values.yaml
    ```

6. Create a secret with data values

   Use `openssl rand -hex 16` to create `SESSION_KEY` and `CLIENT_SECRET` values to be inserted into gangway-data-values.yaml
  
    ```sh
    # Using KUBECONFIG for management cluster get the CA for dex
    kubectl get secret dex-cert-tls -n tanzu-system-auth -o 'go-template={{ index .data "ca.crt" }}'
    ```

    ```sh
    kubectl create secret generic gangway-data-values --from-file=values.yaml=vsphere/gangway-data-values.yaml -n tanzu-system-auth
    ```

7. Deploy gangway extension

    ```sh
    kubectl apply -f gangway-extension.yaml
   ```

8. Retrieve status of an extension

    ```sh
    kubectl get extension gangway -n tanzu-system-auth
    kubectl get app gangway -n tanzu-system-auth
    ```

   Gangway app status should change to `Reconcile Succeeded` once gangway is deployed successfully

   View detailed status

   ```sh
   kubectl get app gangway -n tanzu-system-auth -o yaml
   ```

#### AWS

1. Install TMC's extension manager

    ```sh
    kubectl apply -f tmc-extension-manager.yaml
    ```

2. Install kapp-controller

    ```sh
    kubectl apply -f kapp-controller.yaml
    ```

3. Deploy cert-manager extension

    ```sh
    kubectl apply -f ../../cert-manager/cert-manager-extension.yaml
    ```

4. Create gangway namespace

    ```sh
    kubectl apply -f namespace-role.yaml
    ```

5. Copy `<INFRA_PROVIDER>/gangway-data-values.yaml.example` to `<INFRA_PROVIDER>/<AUTH_PROVIDER>/gangway-data-values.yaml`

   Configure gangway data values in `<INFRA_PROVIDER>/gangway-data-values.yaml`

   Supported configurations are documented in [gangway-configurations](../../../authentication/gangway/README.md)

   *OIDC*:

    ```sh
    cp aws/gangway-data-values.yaml.example aws/gangway-data-values.yaml
    ```

   ***NOTE***:
   * Remove `dns` in gangway-data-values.yaml first and once gangway is deployed, get gangway svc loadbalancer hostname and update `dns` and redeploy gangway extension

6. Create a secret with data values
  
    Use `openssl rand -hex 16` to create `SESSION_KEY` and `CLIENT_SECRET` values to be inserted into gangway-data-values.yaml

    ```sh
    # Using KUBECONFIG for management cluster get the CA for dex
    kubectl get secret dex-cert-tls -n tanzu-system-auth -o 'go-template={{ index .data "ca.crt" }}'
   ```

    ```sh
    kubectl create secret generic gangway-data-values --from-file=values.yaml=aws/gangway-data-values.yaml -n tanzu-system-auth
    ```

7. Deploy gangway extension

    ```sh
    kubectl apply -f gangway-extension.yaml
   ```

8. Retrieve status of an extension

    ```sh
    kubectl get extension gangway -n tanzu-system-auth
    kubectl get app gangway -n tanzu-system-auth
    ```

   Gangway app status should change to `Reconcile Succeeded` once gangway is deployed successfully

   View detailed status

   ```sh
   kubectl get app gangway -n tanzu-system-auth -o yaml
   ```

9. Get gangway service loadbalancer hostname (GANGWAY_SVC_LB_HOSTNAME)

    ```sh
    kubectl get svc gangwaysvc -n tanzu-system-auth -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
    ```

10. Update GANGWAY_SVC_LB_HOSTNAME in gangway-data-values secret and redeploy gangway following [update gangway extension](#update-gangway-extension)

11. Update StaticClients in dex in management cluster with gangway information

12. Redeploy dex on management cluster

### Update gangway extension

1. Get gangway data values from secret

    ```sh
    kubectl get secret gangway-data-values -n tanzu-system-auth -o 'go-template={{ ingangway .data "values.yaml" }}' | base64 -d > gangway-data-values.yaml
    ```

2. Update gangway data values in gangway-data-values.yaml

3. Update gangway data values secret

    ```sh
    kubectl create secret generic gangway-data-values --from-file=values.yaml=gangway-data-values.yaml -n tanzu-system-auth -o yaml --dry-run | kubectl replace -f-
    ```

   Gangway extension will be reconciled again with the above data values

4. Refer to `Retrieve status of an extension` in [deploy gangway extension](#deploy-gangway-extension) to retrieve the status of an extension

### Delete gangway extension

1. Delete gangway extension

    ```sh
    kubectl delete -f gangway-extension.yaml
    kubectl delete app gangway -n tanzu-system-auth
    ```

2. Refer to `Retrieve status of an extension` in [deploy gangway extension](#deploy-gangway-extension) to retrieve the status of an extension

   If extension is deleted successfully, then get of both gangway extension and app should return `Not Found`

3. Delete gangway namespace

   **NOTE: Do not delete namespace-role.yaml before app is deleted fully, as it will lead to errors due to service account used by kapp-controller being deleted**

    ```sh
    kubectl delete -f namespace-role.yaml
    ```

### Upgrade gangway deployment to gangway extension

1. Get gangway configmap

    ```sh
    kubectl get configmap gangway -n tanzu-system-auth -o 'go-template={{ ingangway .data "gangway.yaml" }}' > gangway-configmap.yaml
    ```

2. Delete existing gangway deployment

    ```sh
    kubectl delete namespace tanzu-system-auth
    ```

3. Follow steps in [Deploy gangway extension](#deploy-gangway-extension) to deploy gangway extension

### Test template rendering

1. Test if gangway templates are rendered correctly

    ```sh
    ytt --ignore-unknown-comments -f ../../../common -f ../../../authentication/gangway -f gangway-data-values.yaml
    ```
