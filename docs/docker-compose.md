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

## Run a test

```
                    
$ ./integration-tests/run-tests-for-docker.sh 

[INFO] Scanning for projects...
[INFO] 
[INFO] ------------------< lol.maki.socks:integration-tests >------------------
[INFO] Building integration-tests 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-resources-plugin:3.2.0:resources (default-resources) @ integration-tests ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Using 'UTF-8' encoding to copy filtered properties files.
[INFO] Copying 1 resource
[INFO] Copying 0 resource
[INFO] The encoding used to copy filtered properties files have not been set. This means that the same encoding will be used to copy filtered properties files as when copying other filtered resources. This might not be what you want! Run your build with --debug to see which files might be affected. Read more at https://maven.apache.org/plugins/maven-resources-plugin/examples/filtering-properties-files.html
[INFO] 
[INFO] --- maven-compiler-plugin:3.8.1:compile (default-compile) @ integration-tests ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-resources-plugin:3.2.0:testResources (default-testResources) @ integration-tests ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Using 'UTF-8' encoding to copy filtered properties files.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:3.8.1:testCompile (default-testCompile) @ integration-tests ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-surefire-plugin:2.22.2:test (default-test) @ integration-tests ---
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running lol.maki.socks.IntegrationTestsApplicationTests
2021-01-15 15:43:29.168  INFO [integration-tests,,] 22065 --- [           main] l.m.s.IntegrationTestsApplicationTests   : Starting IntegrationTestsApplicationTests using Java 15 on makinoMacBook-Pro.local with PID 22065 (started by toshiaki in /Users/toshiaki/git/spring-sockshop/integration-tests)
2021-01-15 15:43:29.171 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.IntegrationTestsApplicationTests   : Running with Spring Boot v2.4.1, Spring v5.3.2
2021-01-15 15:43:29.172  INFO [integration-tests,,] 22065 --- [           main] l.m.s.IntegrationTestsApplicationTests   : No active profile set, falling back to default profiles: default
2021-01-15 15:43:30.653  INFO [integration-tests,,] 22065 --- [           main] l.m.s.IntegrationTestsApplicationTests   : Started IntegrationTestsApplicationTests in 1.858 seconds (JVM running for 2.691)
2021-01-15 15:43:31.068 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : --> POST http://localhost:15006/oauth/token
2021-01-15 15:43:31.070 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : Authorization: Basic c29jazpzb2Nr
2021-01-15 15:43:31.289 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : 
2021-01-15 15:43:31.289 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : grant_type=password&username=jdoe&password=demo
2021-01-15 15:43:31.310 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : --> END POST
2021-01-15 15:43:31.420 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : <-- 200 OK http://localhost:15006/oauth/token (347ms)
2021-01-15 15:43:31.421 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : Cache-Control: no-store
2021-01-15 15:43:31.421 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : Pragma: no-cache
2021-01-15 15:43:31.421 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : X-Content-Type-Options: nosniff
2021-01-15 15:43:31.421 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : X-XSS-Protection: 1; mode=block
2021-01-15 15:43:31.422 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : X-Frame-Options: DENY
2021-01-15 15:43:31.422 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : vary: accept-encoding
2021-01-15 15:43:31.422 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : Content-Type: application/json;charset=UTF-8
2021-01-15 15:43:31.422 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : Transfer-Encoding: chunked
2021-01-15 15:43:31.422 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : Date: Fri, 15 Jan 2021 06:43:31 GMT
2021-01-15 15:43:31.446 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : 
2021-01-15 15:43:31.446 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : {"access_token":"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1MDY1ZTIwZC1lN2UwLTRkYmUtOTM4OS1lMTUwMmY0NTYzYzEiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwidXNlcl9uYW1lIjoiamRvZSIsImlzcyI6Imh0dHA6Ly9zb2NrLXVzZXI6NTAwNi9vYXV0aC90b2tlbiIsImdpdmVuX25hbWUiOiJKb2huIiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImNsaWVudF9pZCI6InNvY2siLCJhdWQiOlsic29jayJdLCJzY29wZSI6WyJvcGVuaWQiLCJjYXRhbG9nOnJlYWQiLCJjdXN0b21lcjpyZWFkIiwiY3VzdG9tZXI6d3JpdGUiLCJvcmRlcjpyZWFkIiwib3JkZXI6d3JpdGUiLCJjYXJ0OnJlYWQiLCJjYXJ0OndyaXRlIiwic2hpcHBpbmc6cmVhZCIsInNoaXBwaW5nOndyaXRlIiwicGF5bWVudDphdXRoIl0sIm5hbWUiOiJqZG9lIiwiZXhwIjoxNjEwNzc5NDExLCJpYXQiOjE2MTA2OTMwMTEsImZhbWlseV9uYW1lIjoiRG9lIiwianRpIjoiSkFucmdtM3M3bjB3MWx3UjhjaUwxUWJJM1U0IiwiZW1haWwiOiJqZG9lQGV4YW1wbGUuY29tIn0.iOqZvqJ-bzF124k8y55UI038GkDmDFoExuicKK1qy8Fx2xdICBWx8IQFt4-6akWWXFuVdpW0mLWyPIA9xngk-xkYJXz5xlTXAaJP2gWCdvkL2LkhEB63uuqdDznYUL2FCPwOFl6-z2kVFAofH6g2-c0jeTWU3V13qFhABWvnzbzX5hgYObhT22men9OFo2DhtO9cfePpqump4ro7btg973uWi7VKzX7A6KAFwIfUrGxv3YVKc3G5Jw0k22c1XYHWpDnyIjKth8eT6bkwxmkCn245pAtFhduEH_eX6RTGP7jjDDPZJRhWjlbyTsPJ6kVOjN0pkt6rd6YJyzcpRxaATw","token_type":"bearer","refresh_token":"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1MDY1ZTIwZC1lN2UwLTRkYmUtOTM4OS1lMTUwMmY0NTYzYzEiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwidXNlcl9uYW1lIjoiamRvZSIsImlzcyI6Imh0dHA6Ly9zb2NrLXVzZXI6NTAwNi9vYXV0aC90b2tlbiIsImdpdmVuX25hbWUiOiJKb2huIiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImNsaWVudF9pZCI6InNvY2siLCJhdWQiOlsic29jayJdLCJzY29wZSI6WyJvcGVuaWQiLCJjYXRhbG9nOnJlYWQiLCJjdXN0b21lcjpyZWFkIiwiY3VzdG9tZXI6d3JpdGUiLCJvcmRlcjpyZWFkIiwib3JkZXI6d3JpdGUiLCJjYXJ0OnJlYWQiLCJjYXJ0OndyaXRlIiwic2hpcHBpbmc6cmVhZCIsInNoaXBwaW5nOndyaXRlIiwicGF5bWVudDphdXRoIl0sImF0aSI6IkpBbnJnbTNzN24wdzFsd1I4Y2lMMVFiSTNVNCIsIm5hbWUiOiJqZG9lIiwiZXhwIjoxNjEwNzc5NDExLCJpYXQiOjE2MTA2OTMwMTEsImZhbWlseV9uYW1lIjoiRG9lIiwianRpIjoiNUVzSXU2X1ZkNmtTMFVRd0s0blV5UmVFbnNvIiwiZW1haWwiOiJqZG9lQGV4YW1wbGUuY29tIn0.PjbQ_e3GjkKT1uxvULyOxlcdFCuuxd4tseH6ZBQL7OQF3Dy_Dj7EmF-NztmeDGeJd3bXbhogBZRFwlmlGEjOZzLTEv7vOZu-dF4G61ATIe5QhFrke_HPQZg_wiKuWwE6wL-CAJ1c5IVY2IRk6mBHqJyVFbyIrEeZmgr68GYaMFbo0WZ__In5qImMF_sIBHFhaoMM0ircg24E4fauy8F_wls_9a9BoZADYPUUCSzVV1zOHUDHC6vAtnQgoqUyz1tScbDdtNcKJtE_v2iknrByVr0lJdQP4VT_aZ2mGMzYzjejDrrDu8bVun1zH3qYajdR1labfKhVlfH5TMyfwLqWnA","expires_in":86399,"scope":"openid catalog:read customer:read customer:write order:read order:write cart:read cart:write shipping:read shipping:write payment:auth","id_token":"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1MDY1ZTIwZC1lN2UwLTRkYmUtOTM4OS1lMTUwMmY0NTYzYzEiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwidXNlcl9uYW1lIjoiamRvZSIsImlzcyI6Imh0dHA6Ly9zb2NrLXVzZXI6NTAwNi9vYXV0aC90b2tlbiIsImdpdmVuX25hbWUiOiJKb2huIiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImNsaWVudF9pZCI6InNvY2siLCJhdWQiOlsic29jayJdLCJzY29wZSI6WyJvcGVuaWQiXSwibmFtZSI6Impkb2UiLCJleHAiOjE2MTA3Nzk0MTEsImlhdCI6MTYxMDY5MzAxMSwiZmFtaWx5X25hbWUiOiJEb2UiLCJqdGkiOiJKQW5yZ20zczduMHcxbHdSOGNpTDFRYkkzVTQiLCJlbWFpbCI6Impkb2VAZXhhbXBsZS5jb20ifQ.TJXCp5AFq2oJ30C_MNKbb0UE1AC6GxNi1UxojM4yt_t-5ykHG-WdnOI26WkJwzT6R7gb2tdzy5PiXmHT5ku9ylUOsJNsYdiyolnCwLo-OiWaghZ1F4Hpx2x0cI-CIAjbI-0EWvJENrt48O7gat0VwVugCTZB5Un5ww9EZmSt61esX8A9exhVhEmjrJfB7kI0yh6m0YUnHCf3fjroe7T9S49ZgYDjCMwV5KKOndbSBDidfSo5m4B3VEkxvSARKGQW_W-vJ4XZ9WxuHxqLjkk4paxrpNUB0YHi-9P1FNx4YDZB41R6TdRZqU6cNEeq8GTcxz08Ljav6te-R_bIt4_Log"}
2021-01-15 15:43:31.448 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : <-- END HTTP
2021-01-15 15:43:31.464 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : --> POST http://localhost:15006/oauth/token
2021-01-15 15:43:31.465 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : Authorization: Basic c29jazpzb2Nr
2021-01-15 15:43:31.468 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-3] l.m.s.c.LoggingExchangeFilterFunction    : 
2021-01-15 15:43:31.469 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-3] l.m.s.c.LoggingExchangeFilterFunction    : grant_type=client_credentials
2021-01-15 15:43:31.478 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-3] l.m.s.c.LoggingExchangeFilterFunction    : --> END POST
2021-01-15 15:43:31.491 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-3] l.m.s.c.LoggingExchangeFilterFunction    : <-- 200 OK http://localhost:15006/oauth/token (26ms)
2021-01-15 15:43:31.492 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-3] l.m.s.c.LoggingExchangeFilterFunction    : Cache-Control: no-store
2021-01-15 15:43:31.492 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-3] l.m.s.c.LoggingExchangeFilterFunction    : Pragma: no-cache
2021-01-15 15:43:31.492 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-3] l.m.s.c.LoggingExchangeFilterFunction    : X-Content-Type-Options: nosniff
2021-01-15 15:43:31.492 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-3] l.m.s.c.LoggingExchangeFilterFunction    : X-XSS-Protection: 1; mode=block
2021-01-15 15:43:31.492 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-3] l.m.s.c.LoggingExchangeFilterFunction    : X-Frame-Options: DENY
2021-01-15 15:43:31.492 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-3] l.m.s.c.LoggingExchangeFilterFunction    : vary: accept-encoding
2021-01-15 15:43:31.492 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-3] l.m.s.c.LoggingExchangeFilterFunction    : Content-Type: application/json;charset=UTF-8
2021-01-15 15:43:31.493 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-3] l.m.s.c.LoggingExchangeFilterFunction    : Transfer-Encoding: chunked
2021-01-15 15:43:31.493 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-3] l.m.s.c.LoggingExchangeFilterFunction    : Date: Fri, 15 Jan 2021 06:43:31 GMT
2021-01-15 15:43:31.504 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-3] l.m.s.c.LoggingExchangeFilterFunction    : 
2021-01-15 15:43:31.504 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-3] l.m.s.c.LoggingExchangeFilterFunction    : {"access_token":"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsic29jayJdLCJzY29wZSI6WyJvcGVuaWQiLCJjYXRhbG9nOnJlYWQiLCJjdXN0b21lcjpyZWFkIiwiY3VzdG9tZXI6d3JpdGUiLCJvcmRlcjpyZWFkIiwib3JkZXI6d3JpdGUiLCJjYXJ0OnJlYWQiLCJjYXJ0OndyaXRlIiwic2hpcHBpbmc6cmVhZCIsInNoaXBwaW5nOndyaXRlIiwicGF5bWVudDphdXRoIl0sImlzcyI6Imh0dHA6Ly9zb2NrLXVzZXI6NTAwNi9vYXV0aC90b2tlbiIsImV4cCI6MTYxMDc3OTQxMSwiaWF0IjoxNjEwNjkzMDExLCJhdXRob3JpdGllcyI6WyJST0xFX1RSVVNURURfQ0xJRU5UIl0sImp0aSI6IlpWblNBOUQ5c0FGY0hCWDF1YS0xcXdqM1JuVSIsImNsaWVudF9pZCI6InNvY2sifQ.vyr8QMZq2m3v8Iy5n6Sn6MuVy5TFUd78zLHC6PaySxwHrQ8WSlUdsRdpK1bvS630vRPmzr_eUK5HFjsGk6nlu7p8NUsJntPjNaxm5_vc5kwCCk6xPau_aBtEgaUQiphpbEkajB74LucrBdcwYH-U1gvyuCl1hiApPmBQGmi6cA2t2rol5I1wz2ToxNDVjKjXwiXN_q9EfxYAI39BIkPBBceQF3ztzL_c8mwHhpZyaU1-hP5MidTdnLIZFnpfdpqU2xE7GBBeKP70kewVGp7Grk5NvdWifeAkm4d0QLyr8Nv4b47GJR9qox0GBrH_dlI0metsXqQWz58j2eK4bc0s_A","token_type":"bearer","expires_in":86399,"scope":"openid catalog:read customer:read customer:write order:read order:write cart:read cart:write shipping:read shipping:write payment:auth","id_token":"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsic29jayJdLCJzY29wZSI6WyJvcGVuaWQiXSwiaXNzIjoiaHR0cDovL3NvY2stdXNlcjo1MDA2L29hdXRoL3Rva2VuIiwiZXhwIjoxNjEwNzc5NDExLCJpYXQiOjE2MTA2OTMwMTEsImF1dGhvcml0aWVzIjpbIlJPTEVfVFJVU1RFRF9DTElFTlQiXSwianRpIjoiWlZuU0E5RDlzQUZjSEJYMXVhLTFxd2ozUm5VIiwiY2xpZW50X2lkIjoic29jayJ9.E5d9ABUAFAEEw-M2zwLswumlx_0tfa5Do7MdfBcO_gGEsXgtal29qHXnXmhSQPB3A3JH5uFRgADlU6QaQ-K3ToKq84WVTw7SwmlRleNdk5fTaRQSeCVbFzL4jWGsDTvAp4Gft2ka2bZDyH4k0TZXDCd1-6n3TPUuQQW1Uwzuw4FciqoSURMQJ3UXnOMcLjfcWZJWZqPYwTTN7eMvpUh45IS2BKY9dtAmPHI2fsbmS_NYyiSqIEGesYPTAcTZoLeSTaA1_kTbjLeVGDkAyi_9LDldIGxF_i3rpQc4Vx1ieOVzcGf0nCsV8L5QWhLPfEDla6wFGeiA8CvSjRUgIt6h6g"}
2021-01-15 15:43:31.504 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-3] l.m.s.c.LoggingExchangeFilterFunction    : <-- END HTTP
2021-01-15 15:43:31.507 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : --> DELETE http://localhost:15005/carts/5065e20d-e7e0-4dbe-9389-e1502f4563c1
2021-01-15 15:43:31.508 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsic29jayJdLCJzY29wZSI6WyJvcGVuaWQiLCJjYXRhbG9nOnJlYWQiLCJjdXN0b21lcjpyZWFkIiwiY3VzdG9tZXI6d3JpdGUiLCJvcmRlcjpyZWFkIiwib3JkZXI6d3JpdGUiLCJjYXJ0OnJlYWQiLCJjYXJ0OndyaXRlIiwic2hpcHBpbmc6cmVhZCIsInNoaXBwaW5nOndyaXRlIiwicGF5bWVudDphdXRoIl0sImlzcyI6Imh0dHA6Ly9zb2NrLXVzZXI6NTAwNi9vYXV0aC90b2tlbiIsImV4cCI6MTYxMDc3OTQxMSwiaWF0IjoxNjEwNjkzMDExLCJhdXRob3JpdGllcyI6WyJST0xFX1RSVVNURURfQ0xJRU5UIl0sImp0aSI6IlpWblNBOUQ5c0FGY0hCWDF1YS0xcXdqM1JuVSIsImNsaWVudF9pZCI6InNvY2sifQ.vyr8QMZq2m3v8Iy5n6Sn6MuVy5TFUd78zLHC6PaySxwHrQ8WSlUdsRdpK1bvS630vRPmzr_eUK5HFjsGk6nlu7p8NUsJntPjNaxm5_vc5kwCCk6xPau_aBtEgaUQiphpbEkajB74LucrBdcwYH-U1gvyuCl1hiApPmBQGmi6cA2t2rol5I1wz2ToxNDVjKjXwiXN_q9EfxYAI39BIkPBBceQF3ztzL_c8mwHhpZyaU1-hP5MidTdnLIZFnpfdpqU2xE7GBBeKP70kewVGp7Grk5NvdWifeAkm4d0QLyr8Nv4b47GJR9qox0GBrH_dlI0metsXqQWz58j2eK4bc0s_A
2021-01-15 15:43:31.524 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : --> END DELETE
2021-01-15 15:43:31.545 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : <-- 202 ACCEPTED http://localhost:15005/carts/5065e20d-e7e0-4dbe-9389-e1502f4563c1 (37ms)
2021-01-15 15:43:31.545 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Vary: Origin,Access-Control-Request-Method,Access-Control-Request-Headers
2021-01-15 15:43:31.546 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : X-Content-Type-Options: nosniff
2021-01-15 15:43:31.546 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : X-XSS-Protection: 1; mode=block
2021-01-15 15:43:31.546 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Cache-Control: no-cache, no-store, max-age=0, must-revalidate
2021-01-15 15:43:31.546 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Pragma: no-cache
2021-01-15 15:43:31.546 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Expires: 0
2021-01-15 15:43:31.546 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : X-Frame-Options: DENY
2021-01-15 15:43:31.546 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Content-Length: 0
2021-01-15 15:43:31.546 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Date: Fri, 15 Jan 2021 06:43:31 GMT
2021-01-15 15:43:31.546 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : <-- END HTTP
2021-01-15 15:43:31.588 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : --> GET http://localhost:15001/catalogue
2021-01-15 15:43:31.588 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1MDY1ZTIwZC1lN2UwLTRkYmUtOTM4OS1lMTUwMmY0NTYzYzEiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwidXNlcl9uYW1lIjoiamRvZSIsImlzcyI6Imh0dHA6Ly9zb2NrLXVzZXI6NTAwNi9vYXV0aC90b2tlbiIsImdpdmVuX25hbWUiOiJKb2huIiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImNsaWVudF9pZCI6InNvY2siLCJhdWQiOlsic29jayJdLCJzY29wZSI6WyJvcGVuaWQiLCJjYXRhbG9nOnJlYWQiLCJjdXN0b21lcjpyZWFkIiwiY3VzdG9tZXI6d3JpdGUiLCJvcmRlcjpyZWFkIiwib3JkZXI6d3JpdGUiLCJjYXJ0OnJlYWQiLCJjYXJ0OndyaXRlIiwic2hpcHBpbmc6cmVhZCIsInNoaXBwaW5nOndyaXRlIiwicGF5bWVudDphdXRoIl0sIm5hbWUiOiJqZG9lIiwiZXhwIjoxNjEwNzc5NDExLCJpYXQiOjE2MTA2OTMwMTEsImZhbWlseV9uYW1lIjoiRG9lIiwianRpIjoiSkFucmdtM3M3bjB3MWx3UjhjaUwxUWJJM1U0IiwiZW1haWwiOiJqZG9lQGV4YW1wbGUuY29tIn0.iOqZvqJ-bzF124k8y55UI038GkDmDFoExuicKK1qy8Fx2xdICBWx8IQFt4-6akWWXFuVdpW0mLWyPIA9xngk-xkYJXz5xlTXAaJP2gWCdvkL2LkhEB63uuqdDznYUL2FCPwOFl6-z2kVFAofH6g2-c0jeTWU3V13qFhABWvnzbzX5hgYObhT22men9OFo2DhtO9cfePpqump4ro7btg973uWi7VKzX7A6KAFwIfUrGxv3YVKc3G5Jw0k22c1XYHWpDnyIjKth8eT6bkwxmkCn245pAtFhduEH_eX6RTGP7jjDDPZJRhWjlbyTsPJ6kVOjN0pkt6rd6YJyzcpRxaATw
2021-01-15 15:43:31.599 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-5] l.m.s.c.LoggingExchangeFilterFunction    : --> END GET
2021-01-15 15:43:31.623 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-5] l.m.s.c.LoggingExchangeFilterFunction    : <-- 200 OK http://localhost:15001/catalogue (34ms)
2021-01-15 15:43:31.624 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-5] l.m.s.c.LoggingExchangeFilterFunction    : Vary: origin,access-control-request-method,access-control-request-headers,accept-encoding
2021-01-15 15:43:31.624 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-5] l.m.s.c.LoggingExchangeFilterFunction    : X-Frame-Options: DENY
2021-01-15 15:43:31.624 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-5] l.m.s.c.LoggingExchangeFilterFunction    : Expires: 0
2021-01-15 15:43:31.624 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-5] l.m.s.c.LoggingExchangeFilterFunction    : X-Content-Type-Options: nosniff
2021-01-15 15:43:31.624 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-5] l.m.s.c.LoggingExchangeFilterFunction    : X-XSS-Protection: 1; mode=block
2021-01-15 15:43:31.624 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-5] l.m.s.c.LoggingExchangeFilterFunction    : Cache-Control: no-cache, no-store, max-age=0, must-revalidate
2021-01-15 15:43:31.625 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-5] l.m.s.c.LoggingExchangeFilterFunction    : Pragma: no-cache
2021-01-15 15:43:31.625 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-5] l.m.s.c.LoggingExchangeFilterFunction    : Content-Type: application/json
2021-01-15 15:43:31.625 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-5] l.m.s.c.LoggingExchangeFilterFunction    : Transfer-Encoding: chunked
2021-01-15 15:43:31.625 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-5] l.m.s.c.LoggingExchangeFilterFunction    : Date: Fri, 15 Jan 2021 06:43:31 GMT
2021-01-15 15:43:31.633 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-5] l.m.s.c.LoggingExchangeFilterFunction    : 
2021-01-15 15:43:31.633 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-5] l.m.s.c.LoggingExchangeFilterFunction    : [{"count":115,"description":"For all those leg lovers out there. A perfect example of a swivel chair trained calf. Meticulously trained on a diet of sitting and Pina Coladas. Phwarr...","id":"a0a4f044-b040-410d-8ead-4de0446aec7e","imageUrl":["/images/bit_of_leg_1.jpeg","/images/bit_of_leg_2.jpeg"],"name":"Nerd leg","price":7.99,"tag":["featured","skin","blue"]},{"count":801,"description":"We were not paid to sell this sock. It's just a bit geeky.","id":"d3588630-ad8e-49df-bbd7-3167f7efb246","imageUrl":["/images/youtube_1.jpeg","/images/youtube_2.jpeg"],"name":"YouTube.sock","price":10.99,"tag":["geek","formal"]},{"count":127,"description":"Keep it simple.","id":"abc4f044-b040-410d-8ead-4de0446aec7e","imageUrl":["/images/classic.jpg","/images/classic2.jpg"],"name":"Classic","price":12.00,"tag":["brown","featured","green"]},{"count":808,"description":"enim officia aliqua excepteur esse deserunt quis aliquip nostrud anim","id":"819e1fbf-8b7e-4f6d-811f-693534916a8b","imageUrl":["/images/WAT.jpg","/images/WAT2.jpg"],"name":"Figueroa","price":14.00,"tag":["formal","featured","blue","green"]},{"count":175,"description":"consequat amet cupidatat minim laborum tempor elit ex consequat in","id":"837ab141-399e-4c1f-9abc-bace40296bac","imageUrl":["/images/catsocks.jpg","/images/catsocks2.jpg"],"name":"Cat socks","price":15.00,"tag":["brown","green","formal"]},{"count":820,"description":"Ready for action. Engineers: be ready to smash that next bug! Be ready, with these super-action-sport-masterpieces. This particular engineer was chased away from the office with a stick.","id":"510a0d7e-8e83-4193-b483-e27e09ddc34d","imageUrl":["/images/puma_1.jpeg","/images/puma_2.jpeg"],"name":"SuperSport XL","price":15.00,"tag":["featured","black","sport","formal"]},{"count":33,"description":"Limited issue Weave socks.","id":"6d62d909-f957-430e-8689-b5129c0bb75e","imageUrl":["/images/weave1.jpg","/images/weave2.jpg"],"name":"Weave special","price":17.15,"tag":["geek","black"]},{"count":738,"description":"A mature sock, crossed, with an air of nonchalance.","id":"808a2de1-1aaa-4c25-a9b9-6612e8f29a38","imageUrl":["/images/cross_1.jpeg","/images/cross_2.jpeg"],"name":"Crossed","price":17.32,"tag":["blue","formal","red","action"]},{"count":438,"description":"proident occaecat irure et excepteur labore minim nisi amet irure","id":"3395a43e-2d88-40de-b95f-e00e1502085b","imageUrl":["/images/colourful_socks.jpg","/images/colourful_socks.jpg"],"name":"Colourful","price":18.00,"tag":["brown","featured","blue"]},{"count":30,"description":"Put a little spring in your step","id":"6acfab6b-7574-4082-b37a-5ec94966eaff","imageUrl":["/images/spring_socks_1.jpg","/images/spring_socks_2.jpg"],"name":"Spring","price":21.00,"tag":["geek","green","featured"]}]
2021-01-15 15:43:31.634 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-5] l.m.s.c.LoggingExchangeFilterFunction    : <-- END HTTP
2021-01-15 15:43:31.637 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : --> POST http://localhost:15005/carts/5065e20d-e7e0-4dbe-9389-e1502f4563c1/items
2021-01-15 15:43:31.637 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsic29jayJdLCJzY29wZSI6WyJvcGVuaWQiLCJjYXRhbG9nOnJlYWQiLCJjdXN0b21lcjpyZWFkIiwiY3VzdG9tZXI6d3JpdGUiLCJvcmRlcjpyZWFkIiwib3JkZXI6d3JpdGUiLCJjYXJ0OnJlYWQiLCJjYXJ0OndyaXRlIiwic2hpcHBpbmc6cmVhZCIsInNoaXBwaW5nOndyaXRlIiwicGF5bWVudDphdXRoIl0sImlzcyI6Imh0dHA6Ly9zb2NrLXVzZXI6NTAwNi9vYXV0aC90b2tlbiIsImV4cCI6MTYxMDc3OTQxMSwiaWF0IjoxNjEwNjkzMDExLCJhdXRob3JpdGllcyI6WyJST0xFX1RSVVNURURfQ0xJRU5UIl0sImp0aSI6IlpWblNBOUQ5c0FGY0hCWDF1YS0xcXdqM1JuVSIsImNsaWVudF9pZCI6InNvY2sifQ.vyr8QMZq2m3v8Iy5n6Sn6MuVy5TFUd78zLHC6PaySxwHrQ8WSlUdsRdpK1bvS630vRPmzr_eUK5HFjsGk6nlu7p8NUsJntPjNaxm5_vc5kwCCk6xPau_aBtEgaUQiphpbEkajB74LucrBdcwYH-U1gvyuCl1hiApPmBQGmi6cA2t2rol5I1wz2ToxNDVjKjXwiXN_q9EfxYAI39BIkPBBceQF3ztzL_c8mwHhpZyaU1-hP5MidTdnLIZFnpfdpqU2xE7GBBeKP70kewVGp7Grk5NvdWifeAkm4d0QLyr8Nv4b47GJR9qox0GBrH_dlI0metsXqQWz58j2eK4bc0s_A
2021-01-15 15:43:31.654 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : 
2021-01-15 15:43:31.654 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : {"unitPrice":7,"quantity":1,"itemId":"a0a4f044-b040-410d-8ead-4de0446aec7e"}
2021-01-15 15:43:31.655 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : --> END POST
2021-01-15 15:43:31.673 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : <-- 201 CREATED http://localhost:15005/carts/5065e20d-e7e0-4dbe-9389-e1502f4563c1/items (36ms)
2021-01-15 15:43:31.674 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Vary: origin,access-control-request-method,access-control-request-headers,accept-encoding
2021-01-15 15:43:31.674 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : X-Frame-Options: DENY
2021-01-15 15:43:31.674 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Expires: 0
2021-01-15 15:43:31.674 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : X-Content-Type-Options: nosniff
2021-01-15 15:43:31.674 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : X-XSS-Protection: 1; mode=block
2021-01-15 15:43:31.674 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Cache-Control: no-cache, no-store, max-age=0, must-revalidate
2021-01-15 15:43:31.674 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Pragma: no-cache
2021-01-15 15:43:31.674 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Content-Type: application/json
2021-01-15 15:43:31.674 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Transfer-Encoding: chunked
2021-01-15 15:43:31.674 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Date: Fri, 15 Jan 2021 06:43:31 GMT
2021-01-15 15:43:31.682 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : 
2021-01-15 15:43:31.682 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : {"itemId":"a0a4f044-b040-410d-8ead-4de0446aec7e","quantity":1,"unitPrice":7}
2021-01-15 15:43:31.683 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : <-- END HTTP
2021-01-15 15:43:31.684 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : --> POST http://localhost:15005/carts/5065e20d-e7e0-4dbe-9389-e1502f4563c1/items
2021-01-15 15:43:31.684 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsic29jayJdLCJzY29wZSI6WyJvcGVuaWQiLCJjYXRhbG9nOnJlYWQiLCJjdXN0b21lcjpyZWFkIiwiY3VzdG9tZXI6d3JpdGUiLCJvcmRlcjpyZWFkIiwib3JkZXI6d3JpdGUiLCJjYXJ0OnJlYWQiLCJjYXJ0OndyaXRlIiwic2hpcHBpbmc6cmVhZCIsInNoaXBwaW5nOndyaXRlIiwicGF5bWVudDphdXRoIl0sImlzcyI6Imh0dHA6Ly9zb2NrLXVzZXI6NTAwNi9vYXV0aC90b2tlbiIsImV4cCI6MTYxMDc3OTQxMSwiaWF0IjoxNjEwNjkzMDExLCJhdXRob3JpdGllcyI6WyJST0xFX1RSVVNURURfQ0xJRU5UIl0sImp0aSI6IlpWblNBOUQ5c0FGY0hCWDF1YS0xcXdqM1JuVSIsImNsaWVudF9pZCI6InNvY2sifQ.vyr8QMZq2m3v8Iy5n6Sn6MuVy5TFUd78zLHC6PaySxwHrQ8WSlUdsRdpK1bvS630vRPmzr_eUK5HFjsGk6nlu7p8NUsJntPjNaxm5_vc5kwCCk6xPau_aBtEgaUQiphpbEkajB74LucrBdcwYH-U1gvyuCl1hiApPmBQGmi6cA2t2rol5I1wz2ToxNDVjKjXwiXN_q9EfxYAI39BIkPBBceQF3ztzL_c8mwHhpZyaU1-hP5MidTdnLIZFnpfdpqU2xE7GBBeKP70kewVGp7Grk5NvdWifeAkm4d0QLyr8Nv4b47GJR9qox0GBrH_dlI0metsXqQWz58j2eK4bc0s_A
2021-01-15 15:43:31.686 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : 
2021-01-15 15:43:31.686 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : {"unitPrice":10,"quantity":1,"itemId":"d3588630-ad8e-49df-bbd7-3167f7efb246"}
2021-01-15 15:43:31.687 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : --> END POST
2021-01-15 15:43:31.703 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : <-- 201 CREATED http://localhost:15005/carts/5065e20d-e7e0-4dbe-9389-e1502f4563c1/items (18ms)
2021-01-15 15:43:31.703 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Vary: origin,access-control-request-method,access-control-request-headers,accept-encoding
2021-01-15 15:43:31.703 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : X-Frame-Options: DENY
2021-01-15 15:43:31.704 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Expires: 0
2021-01-15 15:43:31.704 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : X-Content-Type-Options: nosniff
2021-01-15 15:43:31.704 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : X-XSS-Protection: 1; mode=block
2021-01-15 15:43:31.704 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Cache-Control: no-cache, no-store, max-age=0, must-revalidate
2021-01-15 15:43:31.704 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Pragma: no-cache
2021-01-15 15:43:31.704 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Content-Type: application/json
2021-01-15 15:43:31.704 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Transfer-Encoding: chunked
2021-01-15 15:43:31.704 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Date: Fri, 15 Jan 2021 06:43:31 GMT
2021-01-15 15:43:31.706 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : 
2021-01-15 15:43:31.706 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : {"itemId":"d3588630-ad8e-49df-bbd7-3167f7efb246","quantity":1,"unitPrice":10}
2021-01-15 15:43:31.706 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : <-- END HTTP
2021-01-15 15:43:31.708 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : --> PATCH http://localhost:15005/carts/5065e20d-e7e0-4dbe-9389-e1502f4563c1/items
2021-01-15 15:43:31.708 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsic29jayJdLCJzY29wZSI6WyJvcGVuaWQiLCJjYXRhbG9nOnJlYWQiLCJjdXN0b21lcjpyZWFkIiwiY3VzdG9tZXI6d3JpdGUiLCJvcmRlcjpyZWFkIiwib3JkZXI6d3JpdGUiLCJjYXJ0OnJlYWQiLCJjYXJ0OndyaXRlIiwic2hpcHBpbmc6cmVhZCIsInNoaXBwaW5nOndyaXRlIiwicGF5bWVudDphdXRoIl0sImlzcyI6Imh0dHA6Ly9zb2NrLXVzZXI6NTAwNi9vYXV0aC90b2tlbiIsImV4cCI6MTYxMDc3OTQxMSwiaWF0IjoxNjEwNjkzMDExLCJhdXRob3JpdGllcyI6WyJST0xFX1RSVVNURURfQ0xJRU5UIl0sImp0aSI6IlpWblNBOUQ5c0FGY0hCWDF1YS0xcXdqM1JuVSIsImNsaWVudF9pZCI6InNvY2sifQ.vyr8QMZq2m3v8Iy5n6Sn6MuVy5TFUd78zLHC6PaySxwHrQ8WSlUdsRdpK1bvS630vRPmzr_eUK5HFjsGk6nlu7p8NUsJntPjNaxm5_vc5kwCCk6xPau_aBtEgaUQiphpbEkajB74LucrBdcwYH-U1gvyuCl1hiApPmBQGmi6cA2t2rol5I1wz2ToxNDVjKjXwiXN_q9EfxYAI39BIkPBBceQF3ztzL_c8mwHhpZyaU1-hP5MidTdnLIZFnpfdpqU2xE7GBBeKP70kewVGp7Grk5NvdWifeAkm4d0QLyr8Nv4b47GJR9qox0GBrH_dlI0metsXqQWz58j2eK4bc0s_A
2021-01-15 15:43:31.710 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : 
2021-01-15 15:43:31.710 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : {"unitPrice":10,"quantity":3,"itemId":"d3588630-ad8e-49df-bbd7-3167f7efb246"}
2021-01-15 15:43:31.710 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : --> END PATCH
2021-01-15 15:43:31.726 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : <-- 202 ACCEPTED http://localhost:15005/carts/5065e20d-e7e0-4dbe-9389-e1502f4563c1/items (17ms)
2021-01-15 15:43:31.726 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Vary: Origin,Access-Control-Request-Method,Access-Control-Request-Headers
2021-01-15 15:43:31.727 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : X-Content-Type-Options: nosniff
2021-01-15 15:43:31.727 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : X-XSS-Protection: 1; mode=block
2021-01-15 15:43:31.727 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Cache-Control: no-cache, no-store, max-age=0, must-revalidate
2021-01-15 15:43:31.727 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Pragma: no-cache
2021-01-15 15:43:31.727 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Expires: 0
2021-01-15 15:43:31.727 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : X-Frame-Options: DENY
2021-01-15 15:43:31.727 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Content-Length: 0
2021-01-15 15:43:31.727 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Date: Fri, 15 Jan 2021 06:43:31 GMT
2021-01-15 15:43:31.727 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : <-- END HTTP
2021-01-15 15:43:31.729 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : --> GET http://localhost:15005/carts/5065e20d-e7e0-4dbe-9389-e1502f4563c1
2021-01-15 15:43:31.729 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsic29jayJdLCJzY29wZSI6WyJvcGVuaWQiLCJjYXRhbG9nOnJlYWQiLCJjdXN0b21lcjpyZWFkIiwiY3VzdG9tZXI6d3JpdGUiLCJvcmRlcjpyZWFkIiwib3JkZXI6d3JpdGUiLCJjYXJ0OnJlYWQiLCJjYXJ0OndyaXRlIiwic2hpcHBpbmc6cmVhZCIsInNoaXBwaW5nOndyaXRlIiwicGF5bWVudDphdXRoIl0sImlzcyI6Imh0dHA6Ly9zb2NrLXVzZXI6NTAwNi9vYXV0aC90b2tlbiIsImV4cCI6MTYxMDc3OTQxMSwiaWF0IjoxNjEwNjkzMDExLCJhdXRob3JpdGllcyI6WyJST0xFX1RSVVNURURfQ0xJRU5UIl0sImp0aSI6IlpWblNBOUQ5c0FGY0hCWDF1YS0xcXdqM1JuVSIsImNsaWVudF9pZCI6InNvY2sifQ.vyr8QMZq2m3v8Iy5n6Sn6MuVy5TFUd78zLHC6PaySxwHrQ8WSlUdsRdpK1bvS630vRPmzr_eUK5HFjsGk6nlu7p8NUsJntPjNaxm5_vc5kwCCk6xPau_aBtEgaUQiphpbEkajB74LucrBdcwYH-U1gvyuCl1hiApPmBQGmi6cA2t2rol5I1wz2ToxNDVjKjXwiXN_q9EfxYAI39BIkPBBceQF3ztzL_c8mwHhpZyaU1-hP5MidTdnLIZFnpfdpqU2xE7GBBeKP70kewVGp7Grk5NvdWifeAkm4d0QLyr8Nv4b47GJR9qox0GBrH_dlI0metsXqQWz58j2eK4bc0s_A
2021-01-15 15:43:31.730 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : --> END GET
2021-01-15 15:43:31.738 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : <-- 200 OK http://localhost:15005/carts/5065e20d-e7e0-4dbe-9389-e1502f4563c1 (9ms)
2021-01-15 15:43:31.738 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Vary: origin,access-control-request-method,access-control-request-headers,accept-encoding
2021-01-15 15:43:31.739 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : X-Frame-Options: DENY
2021-01-15 15:43:31.739 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Expires: 0
2021-01-15 15:43:31.739 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : X-Content-Type-Options: nosniff
2021-01-15 15:43:31.739 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : X-XSS-Protection: 1; mode=block
2021-01-15 15:43:31.739 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Cache-Control: no-cache, no-store, max-age=0, must-revalidate
2021-01-15 15:43:31.739 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Pragma: no-cache
2021-01-15 15:43:31.739 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Content-Type: application/json
2021-01-15 15:43:31.739 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Transfer-Encoding: chunked
2021-01-15 15:43:31.739 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Date: Fri, 15 Jan 2021 06:43:31 GMT
2021-01-15 15:43:31.740 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : 
2021-01-15 15:43:31.741 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : {"customerId":"5065e20d-e7e0-4dbe-9389-e1502f4563c1","items":[{"itemId":"a0a4f044-b040-410d-8ead-4de0446aec7e","quantity":1,"unitPrice":7.00},{"itemId":"d3588630-ad8e-49df-bbd7-3167f7efb246","quantity":3,"unitPrice":10.00}]}
2021-01-15 15:43:31.741 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : <-- END HTTP
2021-01-15 15:43:31.749 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : --> GET http://localhost:15006/me
2021-01-15 15:43:31.749 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1MDY1ZTIwZC1lN2UwLTRkYmUtOTM4OS1lMTUwMmY0NTYzYzEiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwidXNlcl9uYW1lIjoiamRvZSIsImlzcyI6Imh0dHA6Ly9zb2NrLXVzZXI6NTAwNi9vYXV0aC90b2tlbiIsImdpdmVuX25hbWUiOiJKb2huIiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImNsaWVudF9pZCI6InNvY2siLCJhdWQiOlsic29jayJdLCJzY29wZSI6WyJvcGVuaWQiLCJjYXRhbG9nOnJlYWQiLCJjdXN0b21lcjpyZWFkIiwiY3VzdG9tZXI6d3JpdGUiLCJvcmRlcjpyZWFkIiwib3JkZXI6d3JpdGUiLCJjYXJ0OnJlYWQiLCJjYXJ0OndyaXRlIiwic2hpcHBpbmc6cmVhZCIsInNoaXBwaW5nOndyaXRlIiwicGF5bWVudDphdXRoIl0sIm5hbWUiOiJqZG9lIiwiZXhwIjoxNjEwNzc5NDExLCJpYXQiOjE2MTA2OTMwMTEsImZhbWlseV9uYW1lIjoiRG9lIiwianRpIjoiSkFucmdtM3M3bjB3MWx3UjhjaUwxUWJJM1U0IiwiZW1haWwiOiJqZG9lQGV4YW1wbGUuY29tIn0.iOqZvqJ-bzF124k8y55UI038GkDmDFoExuicKK1qy8Fx2xdICBWx8IQFt4-6akWWXFuVdpW0mLWyPIA9xngk-xkYJXz5xlTXAaJP2gWCdvkL2LkhEB63uuqdDznYUL2FCPwOFl6-z2kVFAofH6g2-c0jeTWU3V13qFhABWvnzbzX5hgYObhT22men9OFo2DhtO9cfePpqump4ro7btg973uWi7VKzX7A6KAFwIfUrGxv3YVKc3G5Jw0k22c1XYHWpDnyIjKth8eT6bkwxmkCn245pAtFhduEH_eX6RTGP7jjDDPZJRhWjlbyTsPJ6kVOjN0pkt6rd6YJyzcpRxaATw
2021-01-15 15:43:31.750 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : --> END GET
2021-01-15 15:43:31.762 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : <-- 200 OK http://localhost:15006/me (13ms)
2021-01-15 15:43:31.763 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : Vary: origin,access-control-request-method,access-control-request-headers,accept-encoding
2021-01-15 15:43:31.763 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : X-Frame-Options: DENY
2021-01-15 15:43:31.763 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : Expires: 0
2021-01-15 15:43:31.763 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : X-Content-Type-Options: nosniff
2021-01-15 15:43:31.763 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : X-XSS-Protection: 1; mode=block
2021-01-15 15:43:31.763 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : Cache-Control: no-cache, no-store, max-age=0, must-revalidate
2021-01-15 15:43:31.763 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : Pragma: no-cache
2021-01-15 15:43:31.763 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : Content-Type: application/json
2021-01-15 15:43:31.763 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : Transfer-Encoding: chunked
2021-01-15 15:43:31.763 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : Date: Fri, 15 Jan 2021 06:43:31 GMT
2021-01-15 15:43:31.765 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : 
2021-01-15 15:43:31.765 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : {"username":"jdoe","firstName":"John","lastName":"Doe","email":"jdoe@example.com","addresses":[{"addressId":"1fb1fe06-32a0-473f-97f2-4a7b70b1d6ca","number":"3401","street":"Hillview Ave","city":"Palo Alto, CA","postcode":"94304","country":"USA"},{"addressId":"afca2d00-a47f-48c2-af48-d5aaa175b35a","number":"501","street":"Second Street Suite 710","city":"San Francisco, CA","postcode":"94107","country":"USA"}],"cards":[{"cardId":"0d32799a-3aaf-446a-b8f2-bea0466a403c","longNum":"4111111111111111","expires":"2025-06-30","ccv":"456"},{"cardId":"8d2b4918-ee2c-49bc-9cb9-625d8a76ad32","longNum":"5555555555554444","expires":"2023-10-31","ccv":"789"}]}
2021-01-15 15:43:31.765 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-2] l.m.s.c.LoggingExchangeFilterFunction    : <-- END HTTP
2021-01-15 15:43:31.766 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : --> POST http://localhost:15004/orders
2021-01-15 15:43:31.767 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1MDY1ZTIwZC1lN2UwLTRkYmUtOTM4OS1lMTUwMmY0NTYzYzEiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwidXNlcl9uYW1lIjoiamRvZSIsImlzcyI6Imh0dHA6Ly9zb2NrLXVzZXI6NTAwNi9vYXV0aC90b2tlbiIsImdpdmVuX25hbWUiOiJKb2huIiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImNsaWVudF9pZCI6InNvY2siLCJhdWQiOlsic29jayJdLCJzY29wZSI6WyJvcGVuaWQiLCJjYXRhbG9nOnJlYWQiLCJjdXN0b21lcjpyZWFkIiwiY3VzdG9tZXI6d3JpdGUiLCJvcmRlcjpyZWFkIiwib3JkZXI6d3JpdGUiLCJjYXJ0OnJlYWQiLCJjYXJ0OndyaXRlIiwic2hpcHBpbmc6cmVhZCIsInNoaXBwaW5nOndyaXRlIiwicGF5bWVudDphdXRoIl0sIm5hbWUiOiJqZG9lIiwiZXhwIjoxNjEwNzc5NDExLCJpYXQiOjE2MTA2OTMwMTEsImZhbWlseV9uYW1lIjoiRG9lIiwianRpIjoiSkFucmdtM3M3bjB3MWx3UjhjaUwxUWJJM1U0IiwiZW1haWwiOiJqZG9lQGV4YW1wbGUuY29tIn0.iOqZvqJ-bzF124k8y55UI038GkDmDFoExuicKK1qy8Fx2xdICBWx8IQFt4-6akWWXFuVdpW0mLWyPIA9xngk-xkYJXz5xlTXAaJP2gWCdvkL2LkhEB63uuqdDznYUL2FCPwOFl6-z2kVFAofH6g2-c0jeTWU3V13qFhABWvnzbzX5hgYObhT22men9OFo2DhtO9cfePpqump4ro7btg973uWi7VKzX7A6KAFwIfUrGxv3YVKc3G5Jw0k22c1XYHWpDnyIjKth8eT6bkwxmkCn245pAtFhduEH_eX6RTGP7jjDDPZJRhWjlbyTsPJ6kVOjN0pkt6rd6YJyzcpRxaATw
2021-01-15 15:43:31.778 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-6] l.m.s.c.LoggingExchangeFilterFunction    : 
2021-01-15 15:43:31.778 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-6] l.m.s.c.LoggingExchangeFilterFunction    : {"addressId":"1fb1fe06-32a0-473f-97f2-4a7b70b1d6ca","cardId":"0d32799a-3aaf-446a-b8f2-bea0466a403c"}
2021-01-15 15:43:31.778 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-6] l.m.s.c.LoggingExchangeFilterFunction    : --> END POST
2021-01-15 15:43:32.005 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-6] l.m.s.c.LoggingExchangeFilterFunction    : <-- 201 CREATED http://localhost:15004/orders (238ms)
2021-01-15 15:43:32.006 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-6] l.m.s.c.LoggingExchangeFilterFunction    : Vary: origin,access-control-request-method,access-control-request-headers,accept-encoding
2021-01-15 15:43:32.006 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-6] l.m.s.c.LoggingExchangeFilterFunction    : X-Frame-Options: DENY
2021-01-15 15:43:32.006 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-6] l.m.s.c.LoggingExchangeFilterFunction    : Expires: 0
2021-01-15 15:43:32.006 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-6] l.m.s.c.LoggingExchangeFilterFunction    : Location: http://localhost:15004/orders/2f762f04
2021-01-15 15:43:32.006 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-6] l.m.s.c.LoggingExchangeFilterFunction    : X-Content-Type-Options: nosniff
2021-01-15 15:43:32.006 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-6] l.m.s.c.LoggingExchangeFilterFunction    : X-XSS-Protection: 1; mode=block
2021-01-15 15:43:32.006 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-6] l.m.s.c.LoggingExchangeFilterFunction    : Cache-Control: no-cache, no-store, max-age=0, must-revalidate
2021-01-15 15:43:32.006 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-6] l.m.s.c.LoggingExchangeFilterFunction    : Pragma: no-cache
2021-01-15 15:43:32.006 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-6] l.m.s.c.LoggingExchangeFilterFunction    : Content-Type: application/json
2021-01-15 15:43:32.006 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-6] l.m.s.c.LoggingExchangeFilterFunction    : Transfer-Encoding: chunked
2021-01-15 15:43:32.006 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-6] l.m.s.c.LoggingExchangeFilterFunction    : Date: Fri, 15 Jan 2021 06:43:32 GMT
2021-01-15 15:43:32.016 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-6] l.m.s.c.LoggingExchangeFilterFunction    : 
2021-01-15 15:43:32.016 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-6] l.m.s.c.LoggingExchangeFilterFunction    : {"id":"2f762f04","customer":{"id":"5065e20d-e7e0-4dbe-9389-e1502f4563c1","firstName":"John","lastName":"Doe","username":"jdoe"},"address":{"number":"3401","street":"Hillview Ave","city":"Palo Alto, CA","postcode":"94304","country":"USA"},"card":{"longNum":"4111111111111111","expires":"2025-06-30","ccv":"456"},"items":[{"itemId":"a0a4f044-b040-410d-8ead-4de0446aec7e","quantity":1,"unitPrice":7.0},{"itemId":"d3588630-ad8e-49df-bbd7-3167f7efb246","quantity":3,"unitPrice":10.0}],"shipment":{"carrier":"USPS","trackingNumber":"f875d4bd-feeb-4a25-89a8-b7354c9f723e","deliveryDate":"2021-01-20"},"date":"2021-01-15T15:43:31.855366+09:00","total":37.0,"status":"CREATED"}
2021-01-15 15:43:32.017 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-6] l.m.s.c.LoggingExchangeFilterFunction    : <-- END HTTP
2021-01-15 15:43:32.021 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : --> GET http://localhost:15003/shipping/2f762f04
2021-01-15 15:43:32.021 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1MDY1ZTIwZC1lN2UwLTRkYmUtOTM4OS1lMTUwMmY0NTYzYzEiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwidXNlcl9uYW1lIjoiamRvZSIsImlzcyI6Imh0dHA6Ly9zb2NrLXVzZXI6NTAwNi9vYXV0aC90b2tlbiIsImdpdmVuX25hbWUiOiJKb2huIiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImNsaWVudF9pZCI6InNvY2siLCJhdWQiOlsic29jayJdLCJzY29wZSI6WyJvcGVuaWQiLCJjYXRhbG9nOnJlYWQiLCJjdXN0b21lcjpyZWFkIiwiY3VzdG9tZXI6d3JpdGUiLCJvcmRlcjpyZWFkIiwib3JkZXI6d3JpdGUiLCJjYXJ0OnJlYWQiLCJjYXJ0OndyaXRlIiwic2hpcHBpbmc6cmVhZCIsInNoaXBwaW5nOndyaXRlIiwicGF5bWVudDphdXRoIl0sIm5hbWUiOiJqZG9lIiwiZXhwIjoxNjEwNzc5NDExLCJpYXQiOjE2MTA2OTMwMTEsImZhbWlseV9uYW1lIjoiRG9lIiwianRpIjoiSkFucmdtM3M3bjB3MWx3UjhjaUwxUWJJM1U0IiwiZW1haWwiOiJqZG9lQGV4YW1wbGUuY29tIn0.iOqZvqJ-bzF124k8y55UI038GkDmDFoExuicKK1qy8Fx2xdICBWx8IQFt4-6akWWXFuVdpW0mLWyPIA9xngk-xkYJXz5xlTXAaJP2gWCdvkL2LkhEB63uuqdDznYUL2FCPwOFl6-z2kVFAofH6g2-c0jeTWU3V13qFhABWvnzbzX5hgYObhT22men9OFo2DhtO9cfePpqump4ro7btg973uWi7VKzX7A6KAFwIfUrGxv3YVKc3G5Jw0k22c1XYHWpDnyIjKth8eT6bkwxmkCn245pAtFhduEH_eX6RTGP7jjDDPZJRhWjlbyTsPJ6kVOjN0pkt6rd6YJyzcpRxaATw
2021-01-15 15:43:32.038 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-7] l.m.s.c.LoggingExchangeFilterFunction    : --> END GET
2021-01-15 15:43:32.056 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-7] l.m.s.c.LoggingExchangeFilterFunction    : <-- 200 OK http://localhost:15003/shipping/2f762f04 (35ms)
2021-01-15 15:43:32.056 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-7] l.m.s.c.LoggingExchangeFilterFunction    : Vary: origin,access-control-request-method,access-control-request-headers,accept-encoding
2021-01-15 15:43:32.056 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-7] l.m.s.c.LoggingExchangeFilterFunction    : X-Frame-Options: DENY
2021-01-15 15:43:32.056 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-7] l.m.s.c.LoggingExchangeFilterFunction    : Expires: 0
2021-01-15 15:43:32.056 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-7] l.m.s.c.LoggingExchangeFilterFunction    : X-Content-Type-Options: nosniff
2021-01-15 15:43:32.056 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-7] l.m.s.c.LoggingExchangeFilterFunction    : X-XSS-Protection: 1; mode=block
2021-01-15 15:43:32.056 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-7] l.m.s.c.LoggingExchangeFilterFunction    : Cache-Control: no-cache, no-store, max-age=0, must-revalidate
2021-01-15 15:43:32.056 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-7] l.m.s.c.LoggingExchangeFilterFunction    : Pragma: no-cache
2021-01-15 15:43:32.056 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-7] l.m.s.c.LoggingExchangeFilterFunction    : Content-Type: application/json
2021-01-15 15:43:32.056 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-7] l.m.s.c.LoggingExchangeFilterFunction    : Transfer-Encoding: chunked
2021-01-15 15:43:32.057 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-7] l.m.s.c.LoggingExchangeFilterFunction    : Date: Fri, 15 Jan 2021 06:43:31 GMT
2021-01-15 15:43:32.073 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-7] l.m.s.c.LoggingExchangeFilterFunction    : 
2021-01-15 15:43:32.073 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-7] l.m.s.c.LoggingExchangeFilterFunction    : {"carrier":"USPS","deliveryDate":"2021-01-20","orderId":"2f762f04","trackingNumber":"f875d4bd-feeb-4a25-89a8-b7354c9f723e"}
2021-01-15 15:43:32.073 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-7] l.m.s.c.LoggingExchangeFilterFunction    : <-- END HTTP
2021-01-15 15:43:32.075 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : --> GET http://localhost:15005/carts/5065e20d-e7e0-4dbe-9389-e1502f4563c1
2021-01-15 15:43:32.075 DEBUG [integration-tests,,] 22065 --- [           main] l.m.s.c.LoggingExchangeFilterFunction    : Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsic29jayJdLCJzY29wZSI6WyJvcGVuaWQiLCJjYXRhbG9nOnJlYWQiLCJjdXN0b21lcjpyZWFkIiwiY3VzdG9tZXI6d3JpdGUiLCJvcmRlcjpyZWFkIiwib3JkZXI6d3JpdGUiLCJjYXJ0OnJlYWQiLCJjYXJ0OndyaXRlIiwic2hpcHBpbmc6cmVhZCIsInNoaXBwaW5nOndyaXRlIiwicGF5bWVudDphdXRoIl0sImlzcyI6Imh0dHA6Ly9zb2NrLXVzZXI6NTAwNi9vYXV0aC90b2tlbiIsImV4cCI6MTYxMDc3OTQxMSwiaWF0IjoxNjEwNjkzMDExLCJhdXRob3JpdGllcyI6WyJST0xFX1RSVVNURURfQ0xJRU5UIl0sImp0aSI6IlpWblNBOUQ5c0FGY0hCWDF1YS0xcXdqM1JuVSIsImNsaWVudF9pZCI6InNvY2sifQ.vyr8QMZq2m3v8Iy5n6Sn6MuVy5TFUd78zLHC6PaySxwHrQ8WSlUdsRdpK1bvS630vRPmzr_eUK5HFjsGk6nlu7p8NUsJntPjNaxm5_vc5kwCCk6xPau_aBtEgaUQiphpbEkajB74LucrBdcwYH-U1gvyuCl1hiApPmBQGmi6cA2t2rol5I1wz2ToxNDVjKjXwiXN_q9EfxYAI39BIkPBBceQF3ztzL_c8mwHhpZyaU1-hP5MidTdnLIZFnpfdpqU2xE7GBBeKP70kewVGp7Grk5NvdWifeAkm4d0QLyr8Nv4b47GJR9qox0GBrH_dlI0metsXqQWz58j2eK4bc0s_A
2021-01-15 15:43:32.077 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : --> END GET
2021-01-15 15:43:32.094 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : <-- 200 OK http://localhost:15005/carts/5065e20d-e7e0-4dbe-9389-e1502f4563c1 (18ms)
2021-01-15 15:43:32.094 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Vary: origin,access-control-request-method,access-control-request-headers,accept-encoding
2021-01-15 15:43:32.094 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : X-Frame-Options: DENY
2021-01-15 15:43:32.094 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Expires: 0
2021-01-15 15:43:32.094 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : X-Content-Type-Options: nosniff
2021-01-15 15:43:32.094 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : X-XSS-Protection: 1; mode=block
2021-01-15 15:43:32.094 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Cache-Control: no-cache, no-store, max-age=0, must-revalidate
2021-01-15 15:43:32.094 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Pragma: no-cache
2021-01-15 15:43:32.094 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Content-Type: application/json
2021-01-15 15:43:32.094 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Transfer-Encoding: chunked
2021-01-15 15:43:32.094 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : Date: Fri, 15 Jan 2021 06:43:31 GMT
2021-01-15 15:43:32.096 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : 
2021-01-15 15:43:32.096 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : {"customerId":"5065e20d-e7e0-4dbe-9389-e1502f4563c1","items":[]}
2021-01-15 15:43:32.096 DEBUG [integration-tests,,] 22065 --- [ctor-http-nio-4] l.m.s.c.LoggingExchangeFilterFunction    : <-- END HTTP
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.701 s - in lol.maki.socks.IntegrationTestsApplicationTests
2021-01-15 15:43:32.131  WARN [integration-tests,,] 22065 --- [extShutdownHook] o.s.b.f.support.DisposableBeanAdapter    : Invocation of destroy method failed on bean with name 'reactorServerResourceFactory': org.springframework.beans.factory.BeanCreationNotAllowedException: Error creating bean with name 'braveTracer': Singleton bean creation not allowed while singletons of this factory are in destruction (Do not request a bean from a BeanFactory in a destroy method implementation!)
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  6.077 s
[INFO] Finished at: 2021-01-15T15:43:32+09:00
[INFO] ------------------------------------------------------------------------
```