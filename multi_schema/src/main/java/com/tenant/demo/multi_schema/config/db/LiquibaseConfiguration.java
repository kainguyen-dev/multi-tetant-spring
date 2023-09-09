package com.tenant.demo.multi_schema.config.db;

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
import org.springframework.context.annotation.DependsOn;
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

    @Bean(name = "liquibaseDatasource")
    public DataSource liquibaseDatasource() {
        if (StringUtils.isNotEmpty(liquibaseDatasource.getPoolName())) {
            return liquibaseDatasource;
        }

        liquibaseDatasource.setUsername(environment.getProperty("database.liquibase.datasource.username"));
        liquibaseDatasource.setPassword(environment.getProperty("database.liquibase.datasource.password"));
        liquibaseDatasource.setJdbcUrl(environment.getProperty("database.datasource.url"));
        liquibaseDatasource.setDriverClassName(environment.getProperty("database.datasource.driverClassName"));
        liquibaseDatasource.setPoolName(environment.getProperty("database.service") + "_LIQUID_BASE_POOL");
        liquibaseDatasource.setMaximumPoolSize(environment.getProperty("database.datasource.maxPoolSize", Integer.class, 32));

        liquibaseDatasource.setLeakDetectionThreshold(60 * 30 * 1000);
        liquibaseDatasource.setMaxLifetime(15 * 60 * 1000);
        liquibaseDatasource.setValidationTimeout(60 * 1000);
        liquibaseDatasource.setConnectionTimeout(60 * 1000);
        liquibaseDatasource.setAutoCommit(false);

        log.info("liquibaseDatasource setup successfully");
        return liquibaseDatasource;
    }

    @Bean("liquibaseForSharedSchema")
    public SpringLiquibase liquibaseForSharedSchema(LiquibaseProperties liquibaseProperties) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(liquibaseDatasource());
        liquibase.setChangeLog(liquibaseProperties.getChangeLog());
        liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
        liquibase.setContexts("shared");
        liquibase.setShouldRun(liquibaseProperties.isEnabled());
        return liquibase;
    }

    @Bean
    @DependsOn("liquibaseForSharedSchema")
    public MultiTenantSpringLiquibase liquibaseForMultiTenant(LiquibaseProperties liquibaseProperties) throws Exception {

        String tenants = environment.getProperty("database.liquibase.tenants");
        if (StringUtils.isEmpty(tenants)) {
            return null;
        }

        MultiTenantSpringLiquibase liquibase = new MultiTenantSpringLiquibase();
        liquibase.setDataSource(liquibaseDatasource());
        liquibase.setChangeLog(liquibaseProperties.getChangeLog());
        liquibase.setSchemas(Arrays.asList(tenants.split(",")));
        liquibase.setContexts("tenant");
        liquibase.setShouldRun(liquibaseProperties.isEnabled());
        liquibase.afterPropertiesSet();
        return liquibase;
    }

}
