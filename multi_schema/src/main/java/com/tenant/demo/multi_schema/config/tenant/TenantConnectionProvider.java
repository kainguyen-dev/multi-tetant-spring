package com.tenant.demo.multi_schema.config.tenant;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

@Component
@Slf4j
public class TenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {
    private final DataSource sharedDataSource;
    private final Map<String, DataSource> tenantDataSources;

    public TenantConnectionProvider(@Qualifier("sharedDatasource") DataSource sharedDataSource,
                                    @Qualifier("tenantDataSources") Map<String, DataSource> tenantDataSources) {
        this.sharedDataSource = sharedDataSource;
        this.tenantDataSources = tenantDataSources;
    }

    @Override
    protected DataSource selectAnyDataSource() {
        return sharedDataSource;
    }

    @Override
    protected DataSource selectDataSource(String tenantId) {
        log.info("Receive request for tenant ID {} fetching data source", tenantId);
        if (tenantDataSources.containsKey(tenantId)) {
            log.info("Found data source for {}", tenantId);
            return tenantDataSources.get(tenantId);
        } else {
            log.error("Can not found data source for {} return default data source", tenantId);
            return sharedDataSource;
        }
    }
}
