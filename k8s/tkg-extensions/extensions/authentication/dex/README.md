# Dex Extension

## Prerequisites

* Workload cluster deployed.
* ytt installed (<https://github.com/k14s/ytt/releases>)
* kapp installed (<https://github.com/k14s/kapp/releases>)

### Deploy dex extension

#### Vsphere

1. Install TMC's extension manager

    ```sh
    kubectl apply -f tmc-extension-manager.yaml
    ```

2. Install kapp-controller

    ```sh
    kubectl apply -f kapp-controller.yaml
    ```

3. Deploy cert-manager if its not already installed(not required for management cluster)

    ```sh
    kubectl apply -f ../../../cert-manager/
    ```

4. Create dex namespace

    ```sh
    kubectl apply -f namespace-role.yaml
    ```

5. Copy `<INFRA_PROVIDER>/<AUTH_PROVIDER>/dex-data-values.yaml.example` to `<INFRA_PROVIDER>/<AUTH_PROVIDER>/dex-data-values.yaml`

   Configure dex data values in `<INFRA_PROVIDER>/<AUTH_PROVIDER>/dex-data-values.yaml`

   Supported configurations are documented in [dex-configurations](../../../authentication/dex/README.md)

    *OIDC*:

    ```sh
    cp vsphere/oidc/dex-data-values.yaml.example vsphere/oidc/dex-data-values.yaml
    ```

    *LDAP*:

    ```sh
    cp vsphere/ldap/dex-data-values.yaml.example vsphere/ldap/dex-data-values.yaml
    ```

   ***NOTE: Remove `staticClients` in dex-data-values.yaml first and once workload cluster is deployed, add `staticClients` and redeploy dex extension***

6. Create a secret with data values after setting appropriate values for params.

    *OIDC*:

    ```sh
    kubectl create secret generic dex-data-values --from-file=values.yaml=vsphere/oidc/dex-data-values.yaml -n tanzu-system-auth
    ```

    *LDAP*:

    ```sh
    kubectl create secret generic dex-data-values --from-file=values.yaml=vsphere/ldap/dex-data-values.yaml -n tanzu-system-auth
    ```

7. Deploy dex extension

    ```sh
    kubectl apply -f dex-extension.yaml
   ```

8. Retrieve status of an extension

    ```sh
    kubectl get extension dex -n tanzu-system-auth
    kubectl get app dex -n tanzu-system-auth
    ```

   Dex app status should change to `Reconcile Succeeded` once dex is deployed successfully

   View detailed status

   ```sh
   kubectl get app dex -n tanzu-system-auth -o yaml
   ```

9. Deploy workload cluster with oidc enabled.

   Set the following env vars

    ```sh
    # replace <VIP> with actual value
    export OIDC_ISSUER_URL=https://<VIP>:30167
    # this is custom based on ldap config
    export OIDC_USERNAME_CLAIM=email
    export OIDC_GROUPS_CLAIM=groups
    # assuming your kubeconfig is now pointing to the management cluster
    export OIDC_DEX_CA=$(kubectl get secret dex-cert-tls -n tanzu-system-auth -o 'go-template={{ index .data "ca.crt" }}' | base64 -d | gzip | base64)
    ```

   Use tkg cli to create workload cluster.

    ```sh
    tkg create cluster workload-cluster-1 --enable-cluster-options="oidc" --plan dev --vsphere-controlplane-endpoint-ip <WORKLOAD_CLUSTER_VIP>
    ```

10. Deploy gangway on workload cluster

11. Update dex-data-values secret with staticClient and redeploy dex following [update dex extension](#update-dex-extension)

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

4. Create dex namespace

    ```sh
    kubectl apply -f namespace-role.yaml
    ```

5. Copy `<INFRA_PROVIDER>/<AUTH_PROVIDER>/dex-data-values.yaml.example` to `<INFRA_PROVIDER>/<AUTH_PROVIDER>/dex-data-values.yaml`

   Configure dex data values in `<INFRA_PROVIDER>/<AUTH_PROVIDER>/dex-data-values.yaml`

   Supported configurations are documented in [dex-configurations](../../../authentication/dex/README.md)

   *OIDC*:

    ```sh
    cp aws/oidc/dex-data-values.yaml.example aws/oidc/dex-data-values.yaml
    ```

   ***NOTE***:
   * Remove `staticClients` in dex-data-values.yaml first and once workload cluster is deployed, add `staticClients` and redeploy dex extension
   * Remove `dns` in dex-data-values.yaml first and once dex is deployed, get dex svc loadbalancer hostname and update `dns` and redeploy dex extension

6. Create a secret with data values

    *OIDC*:

    ```sh
    kubectl create secret generic dex-data-values --from-file=values.yaml=aws/oidc/dex-data-values.yaml -n tanzu-system-auth
    ```

7. Deploy cert-manager extension

    ```sh
    kubectl apply -f ../../cert-manager/cert-manager-extension.yaml
    ```

8. Deploy dex extension

    ```sh
    kubectl apply -f dex-extension.yaml
   ```

9. Retrieve status of an extension

    ```sh
    kubectl get extension dex -n tanzu-system-auth
    kubectl get app dex -n tanzu-system-auth
    ```

   Dex app status should change to `Reconcile Succeeded` once dex is deployed successfully

   View detailed status

   ```sh
   kubectl get app dex -n tanzu-system-auth -o yaml
   ```

10. Get dex service loadbalancer hostname (DEX_SVC_LB_HOSTNAME)

    ```sh
    kubectl get svc dexsvc -n tanzu-system-auth -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
    ```

11. Update DEX_SVC_LB_HOSTNAME in dex-data-values secret and redeploy dex following [update dex extension](#update-dex-extension)

12. Deploy workload cluster with oidc plan by setting env vars as below.

    ```sh
    # replace <DEX_LB> with AWS ELB endpoint of Dex
    export OIDC_ISSUER_URL=https://<DEX_LB>
    # this is custom based on ldap config
    export OIDC_USERNAME_CLAIM=email
    export OIDC_GROUPS_CLAIM=groups
    # assuming your kubeconfig is now pointing to the management cluster
    export OIDC_DEX_CA=$(kubectl get secret dex-cert-tls -n tanzu-system-auth -o 'go-template={{ index .data "ca.crt" }}' | base64 -d | gzip | base64)
    ```

    ```sh
    # Use tkg cli to create workload cluster.
    tkg create cluster workload-cluster-1 --enable-cluster-options="oidc" --plan dev
    ```

13. Deploy gangway on workload cluster

14. Update dex-data-values secret with staticClient and redeploy dex following [update dex extension](#update-dex-extension)

### Update dex extension

1. Get dex data values from secret

    ```sh
    kubectl get secret dex-data-values -n tanzu-system-auth -o 'go-template={{ index .data "values.yaml" }}' | base64 -d > dex-data-values.yaml
    ```

2. Update dex data values in dex-data-values.yaml

3. Update dex data values secret

    ```sh
    kubectl create secret generic dex-data-values --from-file=values.yaml=dex-data-values.yaml -n tanzu-system-auth -o yaml --dry-run | kubectl replace -f-
    ```

   Dex extension will be reconciled again with the above data values

4. Refer to `Retrieve status of an extension` in [deploy dex extension](#deploy-dex-extension) to retrieve the status of an extension

### Delete dex extension

1. Delete dex extension

    ```sh
    kubectl delete -f dex-extension.yaml
    kubectl delete app dex -n tanzu-system-auth
    ```

2. Refer to `Retrieve status of an extension` in [deploy dex extension](#deploy-dex-extension) to retrieve the status of an extension

   If extension is deleted successfully, then get of dex app should return `Not Found`

3. Delete dex namespace

   **NOTE: Do not delete namespace-role.yaml before app is deleted fully, as it will lead to errors due to service account used by kapp-controller being deleted**

    ```sh
    kubectl delete -f namespace-role.yaml
    ```

### Upgrade dex deployment to dex extension

1. Get dex configmap

    ```sh
    kubectl get configmap dex -n tanzu-system-auth -o 'go-template={{ index .data "dex.yaml" }}' > dex-configmap.yaml
    ```

2. Delete existing dex deployment

    ```sh
    kubectl delete namespace tanzu-system-auth
    ```

3. Follow steps in [Deploy dex extension](#deploy-dex-extension) to deploy dex extension

### Test template rendering

1. Test if dex templates are rendered correctly

    ```sh
    ytt --ignore-unknown-comments -f ../../../common -f ../../../authentication/dex -f dex-data-values.yaml
    ```
