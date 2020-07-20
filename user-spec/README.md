# OpenAPI Spec Template

* [ReDoc](https://redocly.github.io/redoc/?url=https://raw.githubusercontent.com/making/spring-sockshop/master/user-spec/openapi/doc.yml)
* [Swagger UI](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/making/spring-sockshop/master/user-spec/openapi/doc.yml)

## Include the generated spec

```xml
<dependency>
    <groupId>lol.maki.socks</groupId>
    <artifactId>user-spec</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <exclusions>
        <exclusion>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger2</artifactId>
        </exclusion>
        <exclusion>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger-ui</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.webjars</groupId>
    <artifactId>swagger-ui</artifactId>
    <version>3.27.0</version>
</dependency>
<dependency>
    <groupId>org.webjars</groupId>
    <artifactId>webjars-locator-core</artifactId>
</dependency>
```

and

```xml
<repositories>
    <repository>
        <id>sonatype-snapshots</id>
        <name>Sonatype Snapshots</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

## How to generate source code from the spec


* Java API Spec

```
./mvnw -V clean generate-sources -f user-spec/pom.xml -P spec
```

* Java Client

```
./mvnw -V clean generate-sources -f user-spec/pom.xml -P client
```
