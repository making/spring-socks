# How to deploy Spring Socks on Cloud Foundry

## Prepare shared service instances

Dummy values should work.

```
cf create-user-provided-service prometheus-rsocket-proxy -p '{"host": "rsocket.example.com", "port": 7001}'
cf create-user-provided-service zipkin -p '{"url": "https://zipkin.example.com"}'
cf create-user-provided-service wavefront -p '{"api-token": "****"}'
```

Deploy Consul Server

```
export APPS_DOMAIN=****
export CF_DOCKER_USERNAME=****
export CF_DOCKER_PASSWORD=****
cf push sock-consul --docker-image consul --docker-username ${CF_DOCKER_USERNAME} -m 256m --no-route --no-start
APP_GUID=$(cf app sock-consul --guid)
cf curl "/v2/apps/${APP_GUID}" -X PUT -d "{\"ports\": [8500]}"
cf create-route ${APPS_DOMAIN} --hostname sock-consul
ROUTE_GUID=$(cf curl "/v2/routes?q=host:sock-consul" | jq -r ".resources[0].metadata.guid")
cf curl /v2/route_mappings -X POST -d "{\"app_guid\": \"${APP_GUID}\", \"route_guid\": \"${ROUTE_GUID}\", \"app_port\": 8500}"
cf start sock-consul

cf create-user-provided-service consul -p "{\"host\": \"sock-consul.${APPS_DOMAIN}\", \"scheme\": \"https\", \"port\": \"443\"}"
```

### Prepare Service URLs

```
cf create-user-provided-service spring-socks -p "{\"url\":\"<frontend url>\"}"
cf create-user-provided-service user-api -p "{\"url\":\"<user-api url>\"}"

Eg.

cf create-user-provided-service spring-socks -p "{\"url\":\"https://spring-socks.apps.pcfone.io\"}"
cf create-user-provided-service user-api -p "{\"url\":\"https://sock-user.apps.pcfone.io\"}"
```

## Deploy User API

```
cf create-service <mysql service> <plan> user-db

Eg.
cf create-service p.mysql db-small user-db
cf create-service shared-mysql shared user-db

or

cf create-user-provided-service user-db -p '{"url": "mysql://<username>:<password>@<host>:<port>/<database>"}' 
```

```
./mvnw clean package -DskipTests -f user-api
cf push -f user-api
```

## Deploy Catalog API

```
cf create-service <mysql service> <plan> catalog-db

Eg.
cf create-service p.mysql db-small catalog-db
cf create-service shared-mysql shared catalog-db

or

cf create-user-provided-service catalog-db -p '{"url": "mysql://<username>:<password>@<host>:<port>/<database>"}' 
```

```
./mvnw clean package -DskipTests -f catalog-api
cf push -f catalog-api
```

## Deploy Cart API

```
cf create-service <mysql service> <plan> cart-db

Eg.
cf create-service p.mysql db-small cart-db
cf create-service shared-mysql shared cart-db

or

cf create-user-provided-service cart-db -p '{"url": "mysql://<username>:<password>@<host>:<port>/<database>"}' 
```

```
./mvnw clean package -DskipTests -f cart-api
cf push -f cart-api
```

## Deploy Payment API

```
./mvnw clean package -DskipTests -f payment-api
cf push -f payment-api
```

## Deploy Shipping API

```
cf create-service <mysql service> <plan> shipping-db

Eg.
cf create-service p.mysql db-small shipping-db
cf create-service shared-mysql shared shipping-db

or

cf create-user-provided-service shipping-db -p '{"url": "mysql://<username>:<password>@<host>:<port>/<database>"}' 
```

```
./mvnw clean package -DskipTests -f shipping-api
cf push -f shipping-api
```

## Deploy Order API

```
cf create-service <mysql service> <plan> order-db

Eg.
cf create-service p.mysql db-small order-db
cf create-service shared-mysql shared order-db

or

cf create-user-provided-service order-db -p '{"url": "mysql://<username>:<password>@<host>:<port>/<database>"}' 
```

```
./mvnw clean package -DskipTests -f order-api
cf push -f order-api
```

## Deploy UI

```
./mvnw clean package -DskipTests -f shop-ui
cf push -f shop-ui
```

![image](https://user-images.githubusercontent.com/106908/104691274-4530fe80-5749-11eb-821e-fd95161ce5a9.png)

You can log in as a demo user (username: `jdoe` / password: `demo`).