# How to run Spring Socks with Docker Compose

```
docker-compose -f integration-tests/docker-compose.yml pull
docker-compose -f integration-tests/docker-compose.yml up
```

wait until sock-ui comes up

```
$ docker ps | grep integration 
07776e66f0c2   ghcr.io/making/spring-socks-ui:latest         "/cnb/process/web"       41 minutes ago   Up 40 minutes             0.0.0.0:8080->6060/tcp                          integration-tests_shop-ui_1
2ce6e1546eaa   ghcr.io/making/spring-socks-order:latest      "/cnb/process/web"       41 minutes ago   Up 41 minutes             0.0.0.0:15004->5004/tcp                         integration-tests_sock-order_1
a53f6c40955a   ghcr.io/making/spring-socks-payment:latest    "/cnb/process/web"       4 hours ago      Up 40 minutes             0.0.0.0:15002->5002/tcp                         integration-tests_sock-payment_1
a239c109e0f5   ghcr.io/making/spring-socks-catalog:latest    "/cnb/process/web"       4 hours ago      Up 40 minutes             0.0.0.0:15001->5001/tcp                         integration-tests_sock-catalog_1
88a78483f9ce   ghcr.io/making/spring-socks-shipping:latest   "/cnb/process/web"       4 hours ago      Up 40 minutes             0.0.0.0:15003->5003/tcp                         integration-tests_sock-shipping_1
75fd6aeb22a6   ghcr.io/making/spring-socks-cart:latest       "/cnb/process/web"       4 hours ago      Up 40 minutes             0.0.0.0:15005->5005/tcp                         integration-tests_sock-cart_1
74c8eaf5ceaa   ghcr.io/making/spring-socks-user:latest       "/cnb/process/web"       4 hours ago      Up 41 minutes             0.0.0.0:15006->5006/tcp                         integration-tests_sock-user_1
bc1a4c0b0486   ghcr.io/openzipkin/zipkin-slim                "start-zipkin"           4 hours ago      Up 41 minutes (healthy)   0.0.0.0:9411->9411/tcp                          integration-tests_zipkin_1
700961c1cb02   ghcr.io/making/mysql:8                        "docker-entrypoint.s…"   4 hours ago      Up 41 minutes             3306/tcp, 33060/tcp                             integration-tests_mysql_1
```

then go to [http://localhost:8080](http://localhost:8080)

![image](https://user-images.githubusercontent.com/106908/104690138-36e1e300-5747-11eb-8466-bce35cc508b1.png)

You can log in as a demo user (username: `jdoe` / password: `demo`).

## Distributed Tracing with Zipkin

Go to [http://localhost:9411](http://localhost:9411)

![image](https://user-images.githubusercontent.com/106908/104690353-a35ce200-5747-11eb-8a26-3a01b7721480.png)

![image](https://user-images.githubusercontent.com/106908/104690384-b2dc2b00-5747-11eb-8c71-506b95266ca4.png)

![image](https://user-images.githubusercontent.com/106908/104690452-cbe4dc00-5747-11eb-8731-48fab004c7fd.png)

