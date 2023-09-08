package com.tenant.demo.multi_schema.config;

import com.tenant.demo.multi_schema.repository.tenants.TenantsRepository;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
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
import java.sql.SQLException;
import java.util.*;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.tenant.demo.multi_schema.repository.tenants"}, repositoryBaseClass = TenantsRepository.class)
@Profile("!test")
@Slf4j
public class TenantJpaConfiguration {

    private final Map<String, DataSource> dataSources = new TreeMap<>();
    private final List<VDatabaseConfigVo> tenants = new ArrayList<>();

    public TenantJpaConfiguration(@Qualifier("sharedDatasource") DataSource sharedDatasource, Environment environment, CurrentTenantIdentifierResolver currentTenantIdentifierResolver) {
        this.sharedDatasource = sharedDatasource;
        this.environment = environment;
        this.currentTenantIdentifierResolver = currentTenantIdentifierResolver;
    }

    private final DataSource sharedDatasource;
    private final Environment environment;
    private final CurrentTenantIdentifierResolver currentTenantIdentifierResolver;

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager() throws SQLException {
        JpaTransactionManager txManager = new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory().getObject()));
        txManager.setDataSource(dataSource());
        log.info("Tenant Transaction Manager setup successfully");
        return txManager;
    }

    @Bean(name = "tenants")
    public List<VDatabaseConfigVo> tenants() throws SQLException {
        String keyVaultUrl = environment.getProperty("database.keyvault.url");
        String secretName = environment.getProperty("database.keyvault.secret.tenants");
        tenants.addAll(VDatabaseConfigUtil.getInstance().getListConfigs(keyVaultUrl, Arrays.asList(secretName.split(VnetConstants.COMMA))));
        return tenants;
    }

    @Bean(name = "tenantDataSources")
    public Map<String, DataSource> tenantDataSources() throws SQLException {
        if (dataSources.isEmpty() && sharedDatasource != null) {
            log.info("Initializing Tenant DataSources");
            tenants().forEach(tenant -> dataSources.put(tenant.getTenantID(), tenantDataSource(tenant)));
            log.info("tenantDataSources(Map<String, DataSource>) bean initialized. Size: " + dataSources.size());
        }

        return dataSources;
    }

    @Bean("entityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws SQLException {
        LocalContainerEntityManagerFactoryBean localBean = new LocalContainerEntityManagerFactoryBean();
        localBean.setPackagesToScan("com.vnet.demandplanning.domain.entity.tenant");

        localBean.setJpaVendorAdapter(jpaVendorAdapter());
        Properties properties = new Properties();

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
        properties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);
        properties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider());
        localBean.setJpaProperties(properties);
        localBean.setDataSource(dataSource());
        return localBean;
    }

    private DataSource tenantDataSource(VDatabaseConfigVo vo) {
        HikariDataSource tenantDatasource = new HikariDataSource();
        tenantDatasource.setUsername(vo.getUsername());
        tenantDatasource.setPassword(vo.getPassword());
        tenantDatasource.setJdbcUrl(environment.getProperty("database.datasource.url"));
        tenantDatasource.setDriverClassName(environment.getProperty("database.datasource.driverClassName"));
        String poolName = environment.getProperty("database.service", "POC_MULTI_TENANT") + "-service-" + vo.getSchema() + "-connection-pool";
        tenantDatasource.setPoolName(poolName);
        tenantDatasource.setMaximumPoolSize(environment.getProperty("database.datasource.maxPoolSize", Integer.class, 32));
        tenantDatasource.setLeakDetectionThreshold(60 * 30 * 1000); // half hour report the sure leak.
        tenantDatasource.setMaxLifetime(15 * 60 * 1000); // The minimum allowed value is 30000ms (30 seconds). Default: 1800000 (30 minutes)
        tenantDatasource.setValidationTimeout(60 * 1000); // 1 mins (default 5s)
        tenantDatasource.setConnectionTimeout(60 * 1000); //  1 mins (default 30s)
        tenantDatasource.setAutoCommit(false);
        log.info("tenantDatasource {} setup successfully", vo.getSchema());
        return tenantDatasource;
    }

    @Bean(name = "dataSource")
    public AbstractRoutingDataSource dataSource() throws SQLException {
        AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return currentTenantIdentifierResolver.resolveCurrentTenantIdentifier();
            }
        };

        Map<Object, Object> dsMap = new HashMap<>(tenantDataSources());

        routingDataSource.setDefaultTargetDataSource(sharedDatasource);
        routingDataSource.setTargetDataSources(dsMap);
        return routingDataSource;
    }

    @Primary
    @Bean(name = "jdbcTemplate")
    public JdbcTemplate jdbcTemplate() throws SQLException {
        return new JdbcTemplate(dataSource());
    }

    @Bean(name = "namedParameterJdbcTemplate")
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate() throws SQLException {
        return new NamedParameterJdbcTemplate(dataSource());
    }

    @Bean
    public MultiTenantConnectionProvider multiTenantConnectionProvider() throws SQLException {
        return new VNetMultiTenantConnectionProvider(sharedDatasource, tenantDataSources());
    }
}
