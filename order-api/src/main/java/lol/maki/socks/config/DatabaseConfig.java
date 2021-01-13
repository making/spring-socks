package lol.maki.socks.config;

import java.util.stream.Stream;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.DatabaseStartupValidator;

/**
 * https://deinum.biz/2020-06-30-Wait-for-database-startup/
 */
@Configuration
public class DatabaseConfig {
	@Bean
	public static BeanFactoryPostProcessor dependsOnPostProcessor() {
		return bf -> {
			final String[] flyway = bf.getBeanNamesForType(Flyway.class);
			Stream.of(flyway)
					.map(bf::getBeanDefinition)
					.forEach(it -> it.setDependsOn("databaseStartupValidator"));
		};
	}

	@Bean
	public DatabaseStartupValidator databaseStartupValidator(DataSource dataSource) {
		final DatabaseStartupValidator dsv = new DatabaseStartupValidator();
		dsv.setDataSource(dataSource);
		return dsv;
	}
}
