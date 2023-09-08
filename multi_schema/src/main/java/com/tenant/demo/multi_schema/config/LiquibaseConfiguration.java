package com.tenant.demo.multi_schema.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import liquibase.integration.spring.MultiTenantSpringLiquibase;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
@EnableConfigurationProperties(LiquibaseProperties.class)
@RequiredArgsConstructor
@Slf4j
public class LiquibaseConfiguration {
    final HikariDataSource liquibaseDatasource = new HikariDataSource();
    private final Environment environment;

    /**
     * Dedicated datasource for liquibase running. The datasource will have specific permissions on
     * shared schema and all tenants.
     * <p>
     * Don't use this datasource outside of liquibase scope.
     */
    @Bean(name = "liquibaseDatasource")
    public DataSource liquibaseDatasource() {
        if (StringUtils.isNotEmpty(liquibaseDatasource.getPoolName())) {
            return liquibaseDatasource;
        }

        liquibaseDatasource.setUsername(environment.getProperty("database.liquibase.datasource.username"));
        liquibaseDatasource.setPassword(environment.getProperty("database.liquibase.datasource.password"));
        liquibaseDatasource.setJdbcUrl(environment.getProperty("database.datasource.url"));
        liquibaseDatasource.setDriverClassName(environment.getProperty("database.datasource.driverClassName"));
        liquibaseDatasource.setPoolName(environment.getProperty("database.service", "POC_MULTI_TENANT") + "-service-liquibase-connection-pool");
        liquibaseDatasource.setMaximumPoolSize(environment.getProperty("database.datasource.maxPoolSize", Integer.class, 32));

        liquibaseDatasource.setLeakDetectionThreshold(60 * 30 * 1000);
        liquibaseDatasource.setMaxLifetime(15 * 60 * 1000);
        liquibaseDatasource.setValidationTimeout(60 * 1000);
        liquibaseDatasource.setConnectionTimeout(60 * 1000);
        liquibaseDatasource.setAutoCommit(false);

        log.info("liquibaseDatasource setup successfully");
        return liquibaseDatasource;
    }

    /**
     * The bean will apply the liquibase changelog to the shared schema when the
     * application start.
     *
     * @param liquibaseProperties
     * @return
     */
    @Bean
    public SpringLiquibase liquibaseForSharedSchema(LiquibaseProperties liquibaseProperties) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(liquibaseDatasource());
        liquibase.setChangeLog(liquibaseProperties.getChangeLog());
        liquibase.setContexts("shared");
        liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
        liquibase.setShouldRun(liquibaseProperties.isEnabled());
        return liquibase;
    }

    /**
     * The bean will apply the liquibase changelog to the tenant schemas when the
     * application start.
     *
     * @param liquibaseProperties
     * @return
     */
    @Bean
    public MultiTenantSpringLiquibase liquibaseForMultiTenant(
            LiquibaseProperties liquibaseProperties) {

        String tenants = environment.getProperty("database.liquibase.tenants");
        if (StringUtils.isEmpty(tenants)) {
            return null;
        }

        MultiTenantSpringLiquibase liquibase = new MultiTenantSpringLiquibase();
        liquibase.setDataSource(liquibaseDatasource());
        liquibase.setContexts("tenant");
        liquibase.setChangeLog(liquibaseProperties.getChangeLog());
        liquibase.setSchemas(Arrays.asList(tenants.split(",")));
        liquibase.setShouldRun(liquibaseProperties.isEnabled());
        return liquibase;
    }

}
