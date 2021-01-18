# Gangway

Gangway is the kubernetes authentication helper to be installed on each workload cluster. It helps with setting up kubeconfig
to access a workload cluster that has been setup to use Dex as the OIDC server.

## vSphere 6.7u3/7.0 setup

### Prerequisites

* YTT installed (<https://github.com/k14s/ytt/releases>).
* Workload cluster deployed.

### Render manifests

Provide config details in `authentication/gangway/values.yaml`. e.g. Replace the following:

| *Parameter* | *Description* | *Default* |
| :------ | ------ | --------- |
| gangway.app | k8s metadata.name | gangway |
| gangway.namespace | k8s metadata.namespace | tanzu-system-auth |
| infrastructure_provider | cloud provider (vsphere or aws) | vsphere |
| gangway.config.clusterName | name of the workload cluster | null |
| gangway.config.MGMT_CLUSTER_IP | VIP of management cluster | null |
| gangway.config.clientID | name of the workload cluster | null |
| gangway.config.APISERVER_URL | VIP of workload cluster | null |
| gangway.config.apiPort | port number of Kubernetes API server endpoint of workload cluster | "6443" |
| gangway.secret.sessionKey | gangway secret sessionKey | null |
| gangway.secret.clientSecret | gangway secret clientSecret | null |
| dns.vsphere.ipAddresses | VIP of workload cluster | `[]` |
| dex.ca | The CA for Dex running on management cluster needs to be provided to Gangway. It should be base64 encoded | null |

*Note:*
To get the dex.ca of dex from management cluster:

```sh
# Using KUBECONFIG for management cluster get the CA for dex
kubectl get secret dex-cert-tls -n tanzu-system-auth -o 'go-template={{ index .data "ca.crt" }}' | base64 -d > dex-ca.crt
```

The file name and path of `dex-ca.crt` should be passed to `dex.ca` if you use ytt command line.

Run ytt command to render manifests based on the values.yaml, passing parameters as example:

#### Workload cluster

1. Install cert-manager on workload cluster if not already installed.

    ```sh
    kubectl apply -f cert-manager/
    ```

2. Create cert, deployment, service, configmap

    ```sh
    CLIENT_SECRET=$(openssl rand -hex 16)
    SESSION_KEY=$(openssl rand -hex 16)
    ytt --ignore-unknown-comments -f common/ -f authentication/gangway/  \
         -v infrastructure_provider=vsphere \
         -v gangway.config.clusterName=workload-cluster-1 \
         -v gangway.config.MGMT_CLUSTER_IP=10.192.213.116 \
         -v gangway.config.clientID=workload-cluster-1 \
         -v gangway.config.APISERVER_URL=10.192.213.73 \
         -v gangway.secret.sessionKey=$SESSION_KEY \
         -v gangway.secret.clientSecret=$CLIENT_SECRET \
         --data-value-file dex.ca=dex-ca.crt \
         --data-value-yaml 'dns.vsphere.ipAddresses=[10.192.213.73]' | kubectl apply -f -
    ```

3. Finally update the configmap of *Dex* in management cluster by adding a new entry to `staticClients` list and bounce the Dex pod by deleting it.

    Replace `<WORKLOAD_CLUSTER_NAME>`, `<WORKLOAD_CLUSTER_IP>` and `<CLIENT_SECRET>` with values from above steps.
    Either use ytt to regenerate config map using example below or manually update configmap.

    ```sh
    ytt --ignore-unknown-comments -f common/ -f authentication/dex/ \
    ....
    ....
    --data-value-yaml 'dex.config.staticClients=[{"id": "<WORKLOAD_CLUSTER_NAME>", "redirectURIs": ["https://<WORKLOAD_CLUSTER_IP>:30166/callback"], "name": "<WORKLOAD_CLUSTER_NAME>", "secret": "<CLIENT_SECRET>"}]' \
    | kubectl apply -f -
    ```

    ```yaml
    staticClients:
    ...
    - id: <WORKLOAD_CLUSTER_NAME>
      redirectURIs:
      - 'https://<WORKLOAD_CLUSTER_VIP>:30166/callback'
      name: '<WORKLOAD_CLUSTER_NAME>'
      secret: $CLIENT_SECRET
    ```

### AWS setup

#### Prerequisites

* YTT installed (<https://github.com/k14s/ytt/releases>).
* Workload cluster deployed.

##### Render manifests

Provide config details in `authentication/gangway/values.yaml`. e.g. Replace the following:

| *Parameter* | *Description* | *Default* |
| :------ | ------ | --------- |
| gangway.app | k8s metadata.name | gangway |
| gangway.namespace | k8s metadata.namespace | tanzu-system-auth |
| infrastructure_provider | cloud provider (vsphere or aws) | vsphere |
| gangway.config.clusterName | name of the workload cluster | null |
| gangway.config.MGMT_CLUSTER_IP | IP of one of control plane nodes of management cluster | null |
| gangway.config.clientID | name of the workload cluster | null |
| gangway.config.APISERVER_URL | IP/DNS of Kubernetes API Server endpoint of workload cluster. This will be the IP address of HAProxy VM. | null |
| gangway.config.apiPort | port number of Kubernetes API server endpoint of workload cluster | "6443" |
| gangway.secret.sessionKey | gangway secret sessionKey | null |
| gangway.secret.clientSecret | gangway secret clientSecret | null |
| dns.aws.GANGWAY_SVC_LB_HOSTNAME | hostname of gangway service loadbalancer in workload cluster | `example.com` |
| gangway.config.DEX_SVC_LB_HOSTNAME | hostname of dex service loadbalancer in management cluster | null |
| dex.ca | The CA for Dex running on management cluster needs to be provided to Gangway. It should be base64 encoded | null |

*Note:*
To get the dex.ca of dex from management cluster:

```sh
# Using KUBECONFIG for management cluster get the CA for dex
kubectl get secret dex-cert-tls -n tanzu-system-auth -o 'go-template={{ index .data "ca.crt" }}' | base64 -d > dex-ca.crt
```

The file name and path of `dex-car.crt` should be passed to `de.ca` if you use ytt command line.

Run ytt command to render manifests based on the values.yaml, passing parameters as example:

```sh
ytt --ignore-unknown-comments -f common/ -f authentication/gangway/  \
     -v infrastructure_provider=aws \
     -v gangway.config.clusterName=workload-cluster-1 \
     -v gangway.config.DEX_SVC_LB_HOSTNAME=dex.lb.svc \
     -v gangway.config.MGMT_CLUSTER_IP=10.160.61.51 \
     -v gangway.config.clientID=workload-cluster-1 \
     -v gangway.config.APISERVER_URL=api.workload.k8s \
     -v gangway.secret.sessionKey=zzzz \
     -v gangway.secret.clientSecret=zzzz \
     --data-value-file dex.ca=dex-ca.crt \
     -v dns.aws.GANGWAY_SVC_LB_HOSTNAME=gangway.lb.svc \
     --output-files .output/authentication/gangway/aws

```

*Note:*

If can not find `GANGWAY_SVC_LB_HOSTNAME`, run the ytt command above to generate `authentication/gangway/aws/02-gangway-service.yaml`, and
Get hostname of gangway service loadbalancer (`GANGWAY_SVC_LB_HOSTNAME`) by command:

```sh
    kubectl apply -f authentication/gangway/aws/01-common.yaml
    kunectl apply -f authentication/gangway/aws/02-gangway-service.yaml
    kubectl get svc gangwaysvc -n tanzu-system-auth -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
```

Use the `GANGWAY_SVC_LB_HOSTNAME` value to replace the one in `authentication/gangway/values.yaml` , and run above ytt command again.

1. Install cert-manager on workload cluster if not already installed.

    ```sh
    kubectl apply -f cert-manager/
    ```

2. Create namespace, service, configmap, secret, cert, deployment

    ```sh
    kubectl apply -f authentication/gangway/aws/
    ```

3. Finally update the configmap of Dex in *management cluster* by adding a new entry to `staticClients` list and bounce the Dex pod by deleting it.
   Replace `<WORKLOAD_CLUSTER_NAME>`, `<GANGWAY_SVC_LB_HOSTNAME>` and `<clientSecret>` with values from above steps.

   ```yaml
    staticClients:
    ...
    - id: <WORKLOAD_CLUSTER_NAME>
      redirectURIs:
      - 'https://<GANGWAY_SVC_LB_HOSTNAME>/callback'
      name: '<WORKLOAD_CLUSTER_NAME>'
      secret: <clientSecret>
   ```

### Azure setup

#### Prerequisites

* YTT installed (<https://github.com/k14s/ytt/releases>).
* Workload cluster deployed.

##### Render manifests

Provide config details in `authentication/gangway/values.yaml`. e.g. Replace the following:

| *Parameter* | *Description* | *Default* |
| :------ | ------ | --------- |
| gangway.app | k8s metadata.name | gangway |
| gangway.namespace | k8s metadata.namespace | tanzu-system-auth |
| infrastructure_provider | cloud provider (vsphere, aws or azure) | vsphere |
| gangway.config.clusterName | name of the workload cluster | null |
| gangway.config.MGMT_CLUSTER_IP | IP of one of control plane nodes of management cluster | null |
| gangway.config.clientID | name of the workload cluster | null |
| gangway.config.APISERVER_URL | IP/DNS of Kubernetes API Server endpoint of workload cluster. This will be the IP address of HAProxy VM. | null |
| gangway.config.apiPort | port number of Kubernetes API server endpoint of workload cluster | "6443" |
| gangway.secret.sessionKey | gangway secret sessionKey | null |
| gangway.secret.clientSecret | gangway secret clientSecret | null |
| dns.azure.GANGWAY_SVC_LB_HOSTNAME | hostname of gangway service loadbalancer in workload cluster | `gangway.example.com` |
| gangway.config.DEX_SVC_LB_HOSTNAME | hostname of dex service loadbalancer in management cluster | null |
| dex.ca | The CA for Dex running on management cluster needs to be provided to Gangway. It should be base64 encoded | null |

*Note:*
To get the dex.ca of dex from management cluster:

```sh
# Using KUBECONFIG for management cluster get the CA for dex
kubectl get secret dex-cert-tls -n tanzu-system-auth -o 'go-template={{ index .data "ca.crt" }}' | base64 -d > dex-ca.crt
```

The file name and path of `dex-car.crt` should be passed to `de.ca` if you use ytt command line.

Run ytt command to render manifests based on the values.yaml, passing parameters as example:

```sh
ytt --ignore-unknown-comments -f common/ -f authentication/gangway/  \
     -v infrastructure_provider=azure \
     -v gangway.config.clusterName=workload-cluster-name \
     -v gangway.config.clientID=tkg-workload-cluster-name \
     -v gangway.config.APISERVER_URL=apiserver_url_tkg_workload_cluster.zone.cloudapp.azure.com \
     -v gangway.secret.sessionKey=zzzz \
     -v gangway.secret.clientSecret=clientSecret \
     -v gangway.config.DEX_SVC_LB_HOSTNAME=dex.example.com \
     -v dns.azure.GANGWAY_SVC_LB_HOSTNAME=gangway.example.com \
     --data-value-file dex.ca=/home/spagno/dex-ca.crt \
     --output-files .output/authentication/gangway/azure
```

1. Install cert-manager on workload cluster if not already installed.

    ```sh
    kubectl apply -f cert-manager/
    ```

2. Create namespace, service, configmap, secret, cert, deployment

    ```sh
    kubectl apply -f .output/authentication/gangway/azure/
    ```

3. Finally update the configmap of Dex in *management cluster* by adding a new entry to `staticClients` list and bounce the Dex pod by deleting it.
   Replace `tkg-workload-cluster-name`, `<GANGWAY_SVC_LB_HOSTNAME>` and `clientSecret` with values from above steps.

   ```yaml
    staticClients:
    ...
    - id: tkg-workload-cluster-name
      redirectURIs:
      - 'https://<GANGWAY_SVC_LB_HOSTNAME>/callback'
      name: 'tkg-workload-cluster-name'
      secret: clientSecret
   ```

*Note:*
Get `apiserver_url_tkg_workload_cluster.zone.cloudapp.azure.com` by command:

```sh
    kubectl config view
```

You have to create `GANGWAY_SVC_LB_HOSTNAME` using the `EXTERNAL-IP` of the `gangwaysvc` service.
Get `EXTERNAL-IP` of gangway service loadBalancer by command:

```sh
    kubectl get svc gangwaysvc -n tanzu-system-auth -o jsonpath='{.status.loadBalancer.ingress[0].ip}'
```

### RBAC setup

The kubeconf provided by gangway will enable user authentication. In order for a user to perform any sort of CRUD action against any kubernetes object
roles and role bindings will need to be defined. See <https://kubernetes.io/docs/reference/access-authn-authz/rbac>

Example cluster role binding that gives any user in an example group cluster-admin access.

```yaml
kind: ClusterRoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: mapbu-admin
subjects:
  - kind: Group
    name: g.rofarrell-org-fte
    apiGroup: ""
roleRef:
  kind: ClusterRole #this must be Role or ClusterRole
  name: cluster-admin # this must match the name of the Role or ClusterRole you wish to bind to
  apiGroup: rbac.authorization.k8s.io
```
