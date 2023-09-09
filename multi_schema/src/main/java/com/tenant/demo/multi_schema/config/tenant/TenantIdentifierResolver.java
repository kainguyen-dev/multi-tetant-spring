package com.tenant.demo.multi_schema.config.tenant;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

import static com.tenant.demo.multi_schema.config.tenant.TenantConstants.SHARED_SCHEMA;

@Component
@Slf4j
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenant = TenantIdContext.getTenantId();
        log.info("Tenant ID Resolver FIND TENANT ID {}", tenant);
        if (StringUtils.isEmpty(tenant)) {
            return SHARED_SCHEMA;
        }
        return tenant;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

}

