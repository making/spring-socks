# TKG Extensions LCM

## Introduction

* TKG Extensions lifecycle is managed by TMC's extension manager and kapp-controller.
* TKG Extensions are packaged as `kind: Extension` objects wrapping `kind: App` objects.
* TMC's extension manager will deploy kapp objects and kapp-controller will deploy the TKG extensions kubernetes objects.

### Prerequisites

* Workload cluster deployed.
* kapp installed (<https://github.com/k14s/kapp/releases>)
* If using daily build, complete steps in [testing daily build](#testing-daily-build)

### Workload cluster

1. Install TMC's extension manager

    ```sh
    kubectl apply -f tmc-extension-manager.yaml
    ```

2. Install kapp-controller

    ```sh
    kubectl apply -f kapp-controller.yaml
    ```

3. Create namespace for extension

    ```sh
    kubectl apply -f namespace-role.yaml
    ```

4. Copy `<extension-name>-data-values.yaml.example` to `<extension-name>-data-values.yaml` and
   Configure data values required for the extension in `<extension-name>-data-values.yaml`

   ```sh
   cp <extension-name>-data-values.yaml.example <extension-name>-data-values.yaml
   ```

5. Create a secret with data values

   ```sh
   kubectl create secret generic <extension-name>-data-values --from-file=values.yaml=<extension-name>-data-values.yaml -n <extension-namespace>
   ```

6. Deploy extensions

    ```sh
    kubectl apply -f <extension-name>-extension.yaml
    ```

7. List apps

    ```sh
    kapp list -n <extension-namespace>
    ```

8. Retrieve status of an extension

    ```sh
    kubectl get extension <extension-name> -n <extension-namespace>
    kubectl get app <extension-name> -n <extension-namespace>
    ```

   App status should change to `Reconcile Succeeded` once extension is deployed successfully

   View detailed status

   ```sh
   kubectl get app <extension-name> -n <extension-namespace> -o yaml
   ```

#### Building template image

Templates are packaged in a docker container which kapp-controller fetches to render and deploy.
If templates are modified, then container needs to be built

1. Build templates

    ```sh
    make build-templates
    make push-templates
    ```

2. Change image in all the extensions yaml to point to this newly built image.

#### Testing daily build

The image registry in daily build points to the final TKG registry `registry.tkg.vmware.run`.
However, the images will not be pushed to the final registry until TKG release's RTM phase.
Below are the steps to modify the image registry to a staging one.

1. Set the staging image registry

    ```sh
    export STAGING_IMAGE_REGISTRY=registry.tkg.vmware.run
    export IMAGE_TAG=v1.2.0_vmware.1
    ```

2. Replace image registry in `tmc-extension-manager.yaml`, `kapp-controller.yaml` and `cert-manager.yaml`

    ```sh
    sed -i -e "s|image: .*/\(.*\):\(.*\)|image: ${STAGING_IMAGE_REGISTRY}/\1:\2|" extensions/tmc-extension-manager.yaml
    rm -rf extensions/tmc-extension-manager.yaml-e

    sed -i -e "s|image: .*/\(.*\):\(.*\)|image: ${STAGING_IMAGE_REGISTRY}/\1:\2|" extensions/kapp-controller.yaml
    rm -rf extensions/kapp-controller.yaml-e

    sed -i -e "s|image: .*/\(.*\)/\(.*\):\(.*\)|image: ${STAGING_IMAGE_REGISTRY}/\1/\2:\3|" cert-manager/*-cert-manager.yaml
    rm -rf cert-manager/*-cert-manager.yaml-e
    ```

3. Replace image url in `<extension-name>-extension.yaml`

    ```sh
    find extensions/ -name *-extension.yaml | xargs sed -i -e "s|url: .*tkg-extensions-templates:.*|url: ${STAGING_IMAGE_REGISTRY}/tkg-extensions-templates:${IMAGE_TAG}|"
    find extensions/ -name *.yaml-e | xargs rm -rf
    ```

4. When deploying extensions, set `image.repository` to `<STAGING_IMAGE_REGISTRY>` in `<extension-name>-data-values.yaml`

   Dex:

    ```yaml
     #@data/values
     #@overlay/match-child-defaults missing_ok=True
     ---
     dex:
       image:
         repository: <STAGING_IMAGE_REGISTRY>
    ```

   Gangway:

    ```yaml
    #@data/values
    #@overlay/match-child-defaults missing_ok=True
    ---
    gangway:
      image:
        repository: <STAGING_IMAGE_REGISTRY>
    ```

   Fluent-bit:

    ```yaml
    #@data/values
    #@overlay/match-child-defaults missing_ok=True
    ---
    logging:
      image:
        repository: <STAGING_IMAGE_REGISTRY>
    ```

    Contour:

    ```yaml
    #@data/values
    #@overlay/match-child-defaults missing_ok=True
    ---
    contour:
      image:
        repository: <STAGING_IMAGE_REGISTRY>
    envoy:
      image:
        repository: <STAGING_IMAGE_REGISTRY>
    ```

    Harbor:

    ```yaml
    #@data/values
    #@overlay/match-child-defaults missing_ok=True
    ---
    image:
      repository: <STAGING_IMAGE_REGISTRY>/harbor
    ```

    Prometheus:

    ```yaml
    #@data/values
    #@overlay/match-child-defaults missing_ok=True
    ---
    monitoring:
      prometheus_server:
        image:
          repository: <STAGING_IMAGE_REGISTRY>/prometheus
      alertmanager:
        image:
          repository: <STAGING_IMAGE_REGISTRY>/prometheus
      kube_state_metrics:
        image:
          repository: <STAGING_IMAGE_REGISTRY>/prometheus
      node_exporter:
        image:
          repository: <STAGING_IMAGE_REGISTRY>/prometheus
      pushgatway:
        image:
          repository: <STAGING_IMAGE_REGISTRY>/prometheus
      cadvisor:
        image:
          repository: <STAGING_IMAGE_REGISTRY>/prometheus
      prometheus_server_configmap_reload:
        image:
          repository: <STAGING_IMAGE_REGISTRY>/prometheus
      prometheus_server_init_container:
        image:
          repository: <STAGING_IMAGE_REGISTRY>/prometheus
    ```

    Grafana:

    ```yaml
    #@data/values
    #@overlay/match-child-defaults missing_ok=True
    ---
    monitoring:
      grafana:
        image:
          repository: <STAGING_IMAGE_REGISTRY>/grafana
      grafana_init_container:
        image:
          repository: <STAGING_IMAGE_REGISTRY>/grafana
      grafana_sc_dashboard:
        image:
          repository: <STAGING_IMAGE_REGISTRY>/grafana
    ```
