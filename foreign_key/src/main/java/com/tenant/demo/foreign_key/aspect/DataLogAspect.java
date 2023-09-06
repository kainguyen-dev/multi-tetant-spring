package com.tenant.demo.foreign_key.aspect;

import com.tenant.demo.foreign_key.config.TenantIdContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class DataLogAspect {
    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* com.tenant.demo.foreign_key.service.DataLogService.*(..))")
    public void beforeExecution() {
        Long tenantId = TenantIdContext.getTenantId();
        if (tenantId != null) {
            log.info("Filter tenantID ... {}", tenantId);
            Session session = entityManager.unwrap(Session.class);
            Filter filter = session.enableFilter("TENANT_FILTER");
            filter.setParameter("tenantId", tenantId);
        }
    }

    @After("execution(* com.tenant.demo.foreign_key.service.DataLogService.*(..))")
    public void afterExecution() {
        Session session = entityManager.unwrap(Session.class);
        session.disableFilter("TENANT_FILTER");
    }
}
