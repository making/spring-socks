package lol.maki.socks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(proxyBeanMethods = false)
@ConfigurationPropertiesScan
public class CatalogApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatalogApiApplication.class, args);
	}

}
