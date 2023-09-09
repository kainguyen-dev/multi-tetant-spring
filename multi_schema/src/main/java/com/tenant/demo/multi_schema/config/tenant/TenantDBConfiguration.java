package com.tenant.demo.multi_schema.config.tenant;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.tenant.demo.multi_schema.repository.tenants"}, entityManagerFactoryRef = "tenantEntityManagerFactory", transactionManagerRef = "tenantTransactionManager")
@RequiredArgsConstructor
@Slf4j
public class TenantDBConfiguration {

    private final Environment environment;
    private final Map<String, DataSource> dataSources = new TreeMap<>();

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean(name = "tenantDataSources")
    public Map<String, DataSource> tenantDataSources(TenantMapProperties tenantMapProperties, @Qualifier("sharedDatasource") DataSource sharedDatasource) {
        if (!tenantMapProperties.getTenants().isEmpty() && sharedDatasource != null) {
            log.info("Initializing Tenant DataSources");
            tenantMapProperties.getTenants().forEach((tenantId, dataSourceProp) -> dataSources.put(tenantId, tenantDataSource(dataSourceProp, tenantId)));
            log.info("tenantDataSources(Map<String, DataSource>) bean initialized. Size: " + dataSources.size());
        }
        dataSources.forEach((s, dataSource) -> log.info("Init data source {} value {}", s, dataSource.toString()));
        return dataSources;
    }

    @Bean(name = "tenantEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean tenantEntityManagerFactory(@Qualifier("tenantDataSource") DataSource dataSource, MultiTenantConnectionProvider multiTenantConnectionProvider, CurrentTenantIdentifierResolver currentTenantIdentifierResolver) {

        LocalContainerEntityManagerFactoryBean entityManager = new LocalContainerEntityManagerFactoryBean();
        entityManager.setPackagesToScan("com.tenant.demo.multi_schema.domain.entity");
        entityManager.setJpaVendorAdapter(this.jpaVendorAdapter());

        Properties properties = new Properties();
        properties.put(org.hibernate.cfg.Environment.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
        properties.put(org.hibernate.cfg.Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);
        properties.setProperty(AvailableSettings.DIALECT, environment.getProperty("hibernate.dialect"));
        properties.setProperty(AvailableSettings.SHOW_SQL, environment.getProperty("hibernate.show_sql"));
        properties.setProperty(AvailableSettings.FORMAT_SQL, environment.getProperty("hibernate.format_sql"));
        properties.setProperty(AvailableSettings.STATEMENT_BATCH_SIZE, environment.getProperty(AvailableSettings.STATEMENT_BATCH_SIZE));
        properties.setProperty(AvailableSettings.ORDER_INSERTS, environment.getProperty(AvailableSettings.ORDER_INSERTS));
        properties.setProperty(AvailableSettings.GLOBALLY_QUOTED_IDENTIFIERS, environment.getProperty("hibernate.globally_quoted_identifiers"));
        properties.setProperty(AvailableSettings.KEYWORD_AUTO_QUOTING_ENABLED, environment.getProperty("hibernate.auto_quote_keyword"));
        properties.setProperty(AvailableSettings.CONNECTION_PROVIDER_DISABLES_AUTOCOMMIT, environment.getProperty("hibernate.connection.provider_disables_autocommit"));
        properties.setProperty(AvailableSettings.USE_SECOND_LEVEL_CACHE, environment.getProperty("hibernate.cache.use_second_level_cache"));
        properties.setProperty(AvailableSettings.USE_QUERY_CACHE, environment.getProperty("hibernate.cache.use_query_cache"));
        properties.setProperty(AvailableSettings.GENERATE_STATISTICS, environment.getProperty("hibernate.generate_statistics"));
        properties.setProperty("hibernate.id.new_generator_mappings", environment.getProperty("hibernate.id.new_generator_mappings"));

        entityManager.setDataSource(dataSource);
        entityManager.setJpaPropertyMap((Map) properties);

        return entityManager;
    }

    @Bean(name = "tenantDataSource")
    public AbstractRoutingDataSource tenantDataSource(@Qualifier("sharedDatasource") DataSource sharedDatasource,
                                                      TenantMapProperties tenantMapProperties,
                                                      CurrentTenantIdentifierResolver currentTenantIdentifierResolver) {
        AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                log.info("LOOKING FOR KEY = {}", currentTenantIdentifierResolver.resolveCurrentTenantIdentifier());
                return currentTenantIdentifierResolver.resolveCurrentTenantIdentifier();
            }
        };
        Map<Object, Object> dsMap = new HashMap<>(tenantDataSources(tenantMapProperties, sharedDatasource));
        routingDataSource.setDefaultTargetDataSource(sharedDatasource);
        routingDataSource.setTargetDataSources(dsMap);
        return routingDataSource;
    }

    private DataSource tenantDataSource(DataSourceProperties tenantMapProperties, String schemaName) {
        HikariDataSource tenantDatasource = new HikariDataSource();
        tenantDatasource.setUsername(tenantMapProperties.getUsername());
        tenantDatasource.setPassword(tenantMapProperties.getPassword());
        tenantDatasource.setJdbcUrl(tenantMapProperties.getUrl());
        tenantDatasource.setDriverClassName(environment.getProperty("database.datasource.driverClassName"));
        tenantDatasource.setPoolName(environment.getProperty("database.service") + "_TENANT_POOL_" + schemaName);
        tenantDatasource.setMaximumPoolSize(environment.getProperty("database.datasource.maxPoolSize", Integer.class, 32));
        tenantDatasource.setSchema(schemaName.toLowerCase());

        tenantDatasource.setLeakDetectionThreshold(60 * 30 * 1000);
        tenantDatasource.setMaxLifetime(15 * 60 * 1000);
        tenantDatasource.setValidationTimeout(60 * 1000);
        tenantDatasource.setConnectionTimeout(60 * 1000);
        tenantDatasource.setAutoCommit(false);
        log.info("TenantDatasource {} setup successfully", schemaName);
        return tenantDatasource;
    }

    @Bean("tenantTransactionManager")
    @Primary
    public PlatformTransactionManager tenantTransactionManager(@Qualifier("tenantEntityManagerFactory") EntityManagerFactory tenantEntityManagerFactory) {
        JpaTransactionManager txManager = new JpaTransactionManager(tenantEntityManagerFactory);
        log.info("tenantTransactionManager setup successfully");
        return txManager;
    }

    @Primary
    @Bean(name = "tenantJdbcTemplate")
    public JdbcTemplate jdbcTemplate(@Qualifier("tenantDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "tenantNamedParameterJdbcTemplate")
    @Primary
    public NamedParameterJdbcTemplate tenantNamedParameterJdbcTemplate(@Qualifier("tenantDataSource") DataSource dataSource) throws SQLException {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}
