package com.tenant.demo.multi_schema.config.tenant;

import lombok.Data;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("database")
@Component
@Data
public class TenantMapProperties {
    Map<String, DataSourceProperties> tenants = new HashMap<>();
}
