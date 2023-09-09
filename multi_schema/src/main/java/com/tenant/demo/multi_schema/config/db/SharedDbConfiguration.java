package com.tenant.demo.multi_schema.config.db;

import com.tenant.demo.multi_schema.config.tenant.TenantConstants;
import com.tenant.demo.multi_schema.repository.shared.SharedRepository;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.tenant.demo.multi_schema.repository.shared"}, repositoryBaseClass = SharedRepository.class,
        entityManagerFactoryRef = "sharedEntityManagerFactory", transactionManagerRef = "sharedTransactionManager")
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class SharedDbConfiguration {

    final HikariDataSource sharedDatasource = new HikariDataSource();
    private final Environment environment;

    @Bean(name = "sharedDatasource")
    public DataSource sharedDatasource() {
        if (StringUtils.isNotEmpty(sharedDatasource.getPoolName())) {
            return sharedDatasource;
        }

        sharedDatasource.setUsername(environment.getProperty("database.shared.username"));
        sharedDatasource.setPassword(environment.getProperty("database.shared.password"));
        sharedDatasource.setJdbcUrl(environment.getProperty("database.shared.url"));
        sharedDatasource.setDriverClassName(environment.getProperty("database.datasource.driverClassName"));
        sharedDatasource.setPoolName(environment.getProperty("database.service") + "_SHARED_POOL");
        sharedDatasource.setMaximumPoolSize(environment.getProperty("database.datasource.maxPoolSize", Integer.class, 32));

        sharedDatasource.setLeakDetectionThreshold(60 * 30 * 1000);
        sharedDatasource.setMaxLifetime(15 * 60 * 1000);
        sharedDatasource.setValidationTimeout(60 * 1000);
        sharedDatasource.setConnectionTimeout(60 * 1000);
        sharedDatasource.setAutoCommit(false);
        sharedDatasource.setSchema(TenantConstants.SHARED_SCHEMA);
        log.info("sharedDatasource setup successfully");
        return sharedDatasource;
    }

    @Bean(name = "sharedJdbcTemplate")
    public JdbcTemplate sharedJdbcTemplate() {
        return new JdbcTemplate(sharedDatasource());
    }

    @Bean(name = "sharedEntityPackages")
    public List<String> sharedEntityPackages() {
        return List.of("com.tenant.demo.multi_schema.repository.shared");
    }

    @Bean(name = "sharedEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean sharedEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean localBean = new LocalContainerEntityManagerFactoryBean();
        localBean.setDataSource(sharedDatasource());
        localBean.setPackagesToScan(sharedEntityPackages().toArray(String[]::new));
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        localBean.setJpaVendorAdapter(vendorAdapter);
        localBean.setJpaProperties(jpaProperties());
        log.info("sharedEntityManagerFactory setup successfully");
        return localBean;
    }

    private Properties jpaProperties() {
        Properties properties = new Properties();
        properties.setProperty(AvailableSettings.DIALECT, environment.getProperty("hibernate.dialect"));
        properties.setProperty(AvailableSettings.SHOW_SQL, environment.getProperty("hibernate.show_sql"));
        properties.setProperty(AvailableSettings.FORMAT_SQL, environment.getProperty("hibernate.format_sql"));
        properties.setProperty(AvailableSettings.GLOBALLY_QUOTED_IDENTIFIERS, environment.getProperty("hibernate.globally_quoted_identifiers"));
        properties.setProperty(AvailableSettings.KEYWORD_AUTO_QUOTING_ENABLED, environment.getProperty("hibernate.auto_quote_keyword"));
        properties.setProperty(AvailableSettings.CONNECTION_PROVIDER_DISABLES_AUTOCOMMIT, environment.getProperty("hibernate.connection.provider_disables_autocommit"));
        properties.setProperty(AvailableSettings.USE_SECOND_LEVEL_CACHE, environment.getProperty("hibernate.cache.use_second_level_cache"));
        properties.setProperty(AvailableSettings.USE_QUERY_CACHE, environment.getProperty("hibernate.cache.use_query_cache"));
        properties.setProperty(AvailableSettings.GENERATE_STATISTICS, environment.getProperty("hibernate.generate_statistics"));
        properties.setProperty("hibernate.id.new_generator_mappings", environment.getProperty("hibernate.id.new_generator_mappings"));
        properties.setProperty(AvailableSettings.DEFAULT_SCHEMA, environment.getProperty("hibernate.default_schema"));
        return properties;
    }

    @Bean("sharedTransactionManager")
    public PlatformTransactionManager sharedTransactionManager() {
        JpaTransactionManager txManager = new JpaTransactionManager(Objects.requireNonNull(sharedEntityManagerFactory().getObject()));
        log.info("sharedTransactionManager setup successfully");
        return txManager;
    }
}
