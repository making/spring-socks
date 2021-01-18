# Authentication with Dex

## Introduction

Dex enables OIDC authentication for kubernetes clusters with external IdP's such as LDAP, SAML, OIDC. Dex will be deployed on the management cluster.

### vSphere 6.7u3/7.0 setup

#### Prerequisites

* YTT installed (<https://github.com/k14s/ytt/releases>).
* Management cluster deployed.

#### Assumptions

* Using self signed certificates.
* Using NodePort for exposing Dex service.

#### LDAP

##### Apply manifests

Set up proper values in `authentication/dex/values.yaml`. Replace the following values:

| *Parameter* | *Description* | *Default* |
| :------ | ------ | --------- |
| dex.app | k8s metadata.name | dex |
| dex.namespace | k8s metadata.namespace | tanzu-system-auth |
| dex.config.connector | dex connector (`ldap` for LDAP) | null |
| dns.vsphere.dnsNames | DNS names list for dex service on vsphere | tkg-dex |
| dns.vsphere.ipAddresses | VIP of management cluster.  | [] |
| dex.config.ldap.host | IP/DNS and port of the LDAP server `<LDAP_HOST>` | null |
| dex.config.ldap.userSearch.* | params that matches with your LDAP server config | null |

##### Management cluster

Run ytt command to render manifests based on the `values.yaml` with example parameters and kubectl apply to management cluster:

```sh
ytt --ignore-unknown-comments -f common/ -f authentication/dex/ \
    -v dex.config.connector=ldap \
    -v dex.config.ldap.host='ldaps.eng.vmware.com:636' \
    -v dex.config.ldap.userSearch.baseDN='ou=people,dc=vmware,dc=com' \
    -v dex.config.ldap.userSearch.filter='(objectClass=posixAccount)' \
    -v dex.config.ldap.userSearch.username=uid \
    -v dex.config.ldap.userSearch.idAttr=uid \
    -v dex.config.ldap.userSearch.emailAttr=mail \
    -v dex.config.ldap.userSearch.nameAttr='givenName' \
    -v dex.config.ldap.groupSearch.baseDN='ou=group,dc=vmware,dc=com' \
    -v dex.config.ldap.groupSearch.filter='(objectClass=posixGroup)' \
    -v dex.config.ldap.groupSearch.userAttr=uid \
    -v dex.config.ldap.groupSearch.groupAttr=memberUid \
    -v dex.config.ldap.groupSearch.nameAttr=cn \
    -v infrastructure_provider=vsphere \
    --data-value-yaml 'dns.vsphere.ipAddresses=[10.192.213.116]' \
     | kubectl apply -f -
```

Note that staticClients can be updated by passing additional param `dex.config.staticClients`, for example:

```sh
    --data-value-yaml 'dex.config.staticClients=[{"id": "workload-cluster-1", "redirectURIs": ["https://10.192.213.73:30166/callback"], "name": "workload-cluster-1", "secret": "7eb3bf7154eaa79363b0557b03be165b"}]' \

```

#### OIDC

##### Apply manifests

Set up proper values in `authentication/dex/values.yaml`. Replace the following values:

| *Parameter* | *Description* | *Default* |
| :------ | ------ | --------- |
| dex.app | k8s metadata.name | dex |
| dex.namespace | k8s metadata.namespace | tanzu-system-auth |
| dex.config.connector | dex connector (`oidc`) | null |
| dns.vsphere.dnsNames | DNS names list for dex service on vsphere | tkg-dex |
| dns.vsphere.ipAddresses | VIP of management cluster.  | [] |
| dex.config.oidc.issuer | `<OIDC_IDP_URL>` with IP/DNS of the OIDC server | null |
| dex.config.oidc.clientID | dex oidc client id env name for oidc secret. No need to change | `$OIDC_CLIENT_ID` |
| dex.config.oidc.clientSecret | dex oidc client secret env name for oidc secret. No need to change | `$OIDC_CLIENT_SECRET` |

Run ytt command to render manifests based on the values.yaml with example parameters:

```sh
ytt --ignore-unknown-comments -f common/ -f authentication/dex/ \
     -v infrastructure_provider=vsphere \
     -v dex.config.connector=oidc \
     -v dex.config.oidc.CLIENT_ID=<INSERT_CLIENT_ID> \
     -v dex.config.oidc.CLIENT_SECRET=<INSERT_CLIENT_SECRET> \
     -v dex.config.oidc.issuer=https://gaz-preview.csp-vidm-prod.com \
     --data-value-yaml 'dns.vsphere.ipAddresses=[10.192.213.116]' | kubectl apply -f -
```

##### Workload cluster enabled with OIDC auth using tkg cli

1. Copy the plan(authentication/dex/plans/cluster-template-oidc-vsphere.yaml) for a workload cluster with OIDC enabled to `.tkg/providers/infrastructure-vsphere/<tkg_version>/`

2. Set the following env vars

    ```sh
    # replace <VIP> with actual value
    export OIDC_ISSUER_URL=https://<VIP>:30167
    # this is custom based on ldap config
    export OIDC_USERNAME_CLAIM=email
    export OIDC_GROUPS_CLAIM=groups
    # assuming your kubeconfig is now pointing to the management cluster
    export OIDC_DEX_CA=$(kubectl get secret dex-cert-tls -n tanzu-system-auth -o 'go-template={{ index .data "ca.crt" }}' | base64 -d | gzip | base64)
    ```

3. Use tkg cli to create workload cluster.

    ```sh
    tkg create cluster workload-cluster-1 --enable-cluster-options="oidc" --plan dev --vsphere-controlplane-endpoint-ip <WORKLOAD_CLUSTER_VIP>
    ```

Next deploy gangway onto the workload cluster using steps outlined in [Gangway](../gangway/README.md)

### AWS setup

#### Prerequisites

* YTT installed (<https://github.com/k14s/ytt/releases>).
* Management cluster deployed.

#### Assumptions

* Using self signed certificates.
* Using service type: LoadBalancer for exposing Dex service.

#### OIDC

##### Render manifests

Set up proper values in `authentication/dex/values.yaml`. Replace values for the following:

| *Parameter* | *Description* | *Default* |
| :------ | ------ | --------- |
| dex.app | k8s metadata.name | dex |
| dex.namespace | k8s metadata.namespace | tanzu-system-auth |
| dex.config.connector | dex connector (`oidc`) | null |
| dns.aws.DEX_SVC_LB_HOSTNAME | `<DEX_SVC_LB_HOSTNAME>` with hostname of dex service loadbalancer | null |
| dns.aws.dnsNames | DNS names list for dex service on AWS | tkg-dex.com |
| dex.config.oidc.issuer | `<OIDC_IDP_URL>` with IP/DNS of the OIDC server | null |
| dex.config.oidc.clientID | dex oidc client id env name for oidc secret. No need to change | `$OIDC_CLIENT_ID` |
| dex.config.oidc.clientSecret | dex oidc client secret env name for oidc secret. No need to change | `$OIDC_CLIENT_SECRET` |

Run ytt command to render manifests based on the values.yaml with example parameters:

```sh
ytt --ignore-unknown-comments -f common/ -f authentication/dex/ \
       -v dex.config.connector=oidc \
       -v dex.config.oidc.CLIENT_ID=abc \
       -v dex.config.oidc.CLIENT_SECRET=sfk \
       -v dex.config.oidc.issuer=https://gaz-preview.csp-vidm-prod.com \
       -v dns.aws.DEX_SVC_LB_HOSTNAME=dex.svc.lb.aws \
       -v infrastructure_provider=aws | kubectl apply -f -

```

*Note:*

If can not find `DEX_SVC_LB_HOSTNAME`, run the ytt command above to generate `authentication/dex/aws/06-service.yaml`, and
Get hostname of dex service loadbalancer (`DEX_SVC_LB_HOSTNAME`) by command:

```sh
    kubectl apply -f authentication/dex/aws/01-common.yaml
    kunectl apply -f authentication/dex/aws/06-service.yaml
    kubectl get svc dexsvc -n tanzu-system-auth -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
```

Use the `DEX_SVC_LB_HOSTNAME` value to replace the one in `authentication/dex/values.yaml` , and run above ytt command again.

##### Management cluster

1. Install cert-manager on management cluster if not already installed.

    ```sh
    kubectl apply -f cert-manager/
    ```

2. Create namespace, Service Account, cert, configMap, rbac, deployment and service

    ```sh
    kubectl apply -f authentication/dex/aws/
    ```

##### Workload cluster enabled with OIDC auth using tkg cli

1. Copy the plan(authentication/dex/plans/cluster-template-oidc-aws.yaml) for a workload cluster with OIDC enabled to `.tkg/providers/infrastructure-aws/<tkg_version>/`

2. Set the following env vars

    ```sh
    # replace <DEX_SVC_LB_HOSTNAME> with actual value
    export OIDC_ISSUER_URL=https://<DEX_SVC_LB_HOSTNAME>
    # this is custom based on ldap config
    export OIDC_USERNAME_CLAIM=email
    export OIDC_GROUPS_CLAIM=groups
    # assuming your kubeconfig is now pointing to the management cluster
    export OIDC_DEX_CA=$(kubectl get secret dex-cert-tls -n tanzu-system-auth -o 'go-template={{ index .data "ca.crt" }}' | base64 -d | gzip | base64)
    ```

3. Use tkg cli to create workload cluster.

    ```sh
    tkg create cluster my-cluster --plan=oidc
    ```

Next deploy gangway onto the workload cluster using steps outlined in [Gangway](../gangway/README.md)

### Azure setup

#### Prerequisites

* YTT installed (<https://github.com/k14s/ytt/releases>).
* Azure AD DS deployed.
* Secure LDAP enabled.
* Management cluster deployed.

#### Assumptions

* Using self signed certificates.
* Using LoadBalancer for exposing Dex service.

#### Secure LDAP Azure AD DS

##### Render manifests

Set up proper values in `authentication/dex/values.yaml`. Replace the following values:

| *Parameter* | *Description* | *Default* |
| :------ | ------ | --------- |
| dex.app | k8s metadata.name | dex |
| dex.namespace | k8s metadata.namespace | tanzu-system-auth |
| dex.config.connector | dex connector (`ldap` for LDAP) | null |
| dns.azure.DEX_SVC_LB_HOSTNAME | `<DEX_SVC_LB_HOSTNAME>` with hostname of dex service loadbalancer | dex.example.com |
| dex.config.ldap.host | IP/DNS and port of the LDAP server `<LDAP_HOST>` | null |
| dex.config.ldap.userSearch.* | params that matches with your LDAP server config | null |

Run ytt command to render manifests based on the `values.yaml` with example parameters:

```sh
ROOTCA_DATA=$(cat adds.cer | base64 -w0 )
ytt --ignore-unknown-comments -f common/ -f authentication/dex/ \
    -v dex.config.connector=ldap \
    -v dex.config.ldap.host='ldaps.example.com' \
    -v dex.config.ldap.userSearch.baseDN='dc=example,dc=com' \
    -v dex.config.ldap.bindDN='CN=administrator,OU=AADDC Users,DC=example,DC=com' \
    -v dex.config.ldap.bindPW='Password1!' \
    -v dex.config.ldap.userSearch.filter='(objectClass=person)' \
    -v dex.config.ldap.userSearch.username=userPrincipalName \
    -v dex.config.ldap.userSearch.idAttr=DN \
    -v dex.config.ldap.userSearch.emailAttr=userPrincipalName \
    -v dex.config.ldap.userSearch.nameAttr='cn' \
    -v dex.config.ldap.groupSearch.baseDN='dc=example,dc=com' \
    -v dex.config.ldap.groupSearch.filter='(objectClass=group)' \
    -v dex.config.ldap.groupSearch.userAttr=DN \
    -v dex.config.ldap.groupSearch.groupAttr='member:1.2.840.113556.1.4.1941:' \
    -v dex.config.ldap.groupSearch.nameAttr=cn \
    -v infrastructure_provider=azure \
    -v dns.azure.DEX_SVC_LB_HOSTNAME=dex.example.com \
    -v dex.config.ldap.rootCAData="$ROOTCA_DATA" \
    --output-files .output/authentication/dex/azure/
```

*Note:*

If you are using `Secure LDAP Azure AD DS` with a ssl certificate signed by a public CA
(like Let's Encrypt) you don't need to add the `dex.config.ldap.rootCAData="$ROOTCA_DATA"`

##### Management cluster

1. Install cert-manager on management cluster if not already installed.

    ```sh
    kubectl apply -f cert-manager/
    ```

2. Create namespace, Service Account, cert, configMap, rbac, deployment and service

    ```sh
    kubectl apply -f .output/authentication/dex/azure/
    ```

*Note:*

You have to create `DEX_SVC_LB_HOSTNAME` using the `EXTERNAL-IP` of the `dexsvc` service.
Get `EXTERNAL-IP` of dex service loadBalancer by command:

```sh
    kubectl get svc dexsvc -n tanzu-system-auth -o jsonpath='{.status.loadBalancer.ingress[0].ip}'
```

##### Workload cluster enabled with OIDC auth using tkg cli

1. Copy the plan(authentication/plans/cluster-template-oidc-azure.yaml) for a workload cluster with OIDC enabled to `~/.tkg/providers/infrastructure-azure/<tkg_version>/`

2. Set the following env vars

    ```sh
    # replace <DEX_SVC_LB_HOSTNAME> with actual value
    export OIDC_ISSUER_URL=https://<DEX_SVC_LB_HOSTNAME>
    # this is custom based on ldap config
    export OIDC_USERNAME_CLAIM=email
    export OIDC_GROUPS_CLAIM=groups
    # assuming your kubeconfig is now pointing to the management cluster
    export OIDC_DEX_CA=$(kubectl get secret dex-cert-tls -n tanzu-system-auth -o 'go-template={{ index .data "ca.crt" }}' | base64 -d | gzip | base64)
    ```

3. Use tkg cli to create workload cluster.

    ```sh
    tkg create cluster my-cluster --plan=oidc
    ```

Next deploy gangway onto the workload cluster using steps outlined in [Gangway](../gangway/README.md)
