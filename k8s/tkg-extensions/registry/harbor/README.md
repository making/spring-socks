# Harbor Docker Registry

## Introduction

[Harbor](https://github.com/goharbor/harbor) is an open source trusted cloud native registry project that stores, signs, and scans content. Harbor extends the open source Docker Distribution by adding the functionalities usually required by users such as security, identity and management.

## Deploying Harbor

### Prerequisites

* YTT installed (<https://github.com/k14s/ytt/releases>).
* Workload Cluster deployed.

### Workload cluster

1. Install cert-manager on the Workload Cluster if not already installed.

    ```sh
    kubectl apply -f cert-manager/
    ```

2. Install Contour on the Workload Cluster if not already installed. Follow [Contour's README](../../ingress/contour/README.md) to install Contour.

3. Create ytt data values file config.yaml, or simply copy from [values.yaml](values.yaml).

   ```yaml
   #@data/values
   ---
   <key1>:<value1>
   <key2>:<value2>
   ```

4. Manually specify the mandatory passwords and secrets in config.yaml, or run `bash scripts/generate-passwords.sh config.yaml` to generate them automatically. This step is needed only once.

5. [Optional] Manually specify other Harbor configuration (e.g. admin password, hostname, persistence setting, etc.) in config.yaml.

6. Deploy Harbor

    ```sh
      ytt --ignore-unknown-comments -f common/ -f registry/harbor/ -f config.yaml | kubectl apply -f-
    ```

## How to generate the manifests from harbor-helm

The manifests from 02 to 11 are generated from [harbor-helm](https://github.com/goharbor/harbor-helm). Helm CLI 3 and [yq](https://github.com/mikefarah/yq) are required to generate these manifests.

1. Clone the harbor-helm repo.

    ```sh
    git clone https://github.com/goharbor/harbor-helm.git
    ```

2. Create a script named `generate-manifests.sh` in the harbor-helm directory.

    ```shell
    #!/usr/bin/env bash
    rm -rf manifests
    mkdir -p manifests

    sed -i 's/"%s-%s" .Release.Name $name/"harbor"/' templates/_helpers.tpl
    valuesFile=$(mktemp /tmp/values.XXXXXX.yaml)
    release=myharbor

    cat <<EOF >> $valuesFile
    expose:
      tls:
        secretName: harbor-tls
    chartmuseum:
      enabled: false
    secretKey: -the-secret-key-
    core:
      secret: the-secret-of-the-core
      xsrfKey: -xsrf-key-must-be-32-characters-
      secretName: harbor-token-service
    jobservice:
      secret: the-secret-of-the-jobservice
    notary:
      secretName: harbor-notary-signer
    registry:
      secret: the-secret-of-the-registry
    internalTLS:
      enabled: true
      certSource: secret
      core:
        secretName: harbor-core-internal-tls
      jobservice:
        secretName: harbor-jobservice-internal-tls
      registry:
        secretName: harbor-registry-internal-tls
      portal:
        secretName: harbor-portal-internal-tls
      chartmuseum:
        secretName: harbor-chartmuseum-internal-tls
      clair:
        secretName: harbor-clair-internal-tls
      trivy:
        secretName: harbor-trivy-internal-tls
    EOF

    ix=2
    for item in `ls templates`; do
      if [ -d templates/$item ]; then
        filename="$(printf "manifests/%02d-%s.yaml" $ix $item)"
        for subitem in `ls templates/$item`; do
          content=`helm template $release . -s templates/$item/$subitem -f $valuesFile`
          content=`echo "$content" | sed '/^# Source: /d'`
          content=`echo "$content" | yq d - -d'*' **.selector.release`
          content=`echo "$content" | yq d - -d'*' **.selector.matchLabels.release`
          content=`echo "$content" | yq d - -d'*' **.labels.heritage`
          content=`echo "$content" | yq d - -d'*' **.labels.chart`
          content=`echo "$content" | yq d - -d'*' **.labels.release`
          if [[ $item != "ingress" ]]; then
            content=`echo "$content" | yq d - -d'*' **.annotations`
          fi
          if [[ $content != "{}"  ]]; then
            if [[ $content = *[!\ ]* ]]; then
              content=`echo "$content" | yq w - -d'*' 'metadata.namespace' tanzu-system-registry`
              echo '---' >> $filename
              echo "$content" >> $filename
            fi
          fi
        done

        if [[ -z $(grep '[^[:space:]]' $filename) ]]; then
            rm -rf $filename
        else
            ((ix=ix+1))
        fi
      fi
    done

    rm $valuesFile

    git checkout templates/_helpers.tpl
    ```

3. Run `generate-manifests.sh` to generate the manifests and the output files are in the `manifests` directory.

    ```sh
    chmod +x ./generate-manifests.sh
    ./generate-manifests.sh
    ```
