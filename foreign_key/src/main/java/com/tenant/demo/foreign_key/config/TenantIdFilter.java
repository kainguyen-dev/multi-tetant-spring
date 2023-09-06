package com.tenant.demo.foreign_key.config;

import jakarta.servlet.*;

import java.io.IOException;

import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebFilter(urlPatterns = "/*", dispatcherTypes = {DispatcherType.REQUEST})
public class TenantIdFilter implements Filter {
    private final String TENANT_ID_HEADER = "X-TENANT-ID";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        log.info("Setting tenantID ...");
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String tenantId = request.getHeader(TENANT_ID_HEADER);
        log.info("New request Tenant ID {}", tenantId);
        if (tenantId == null){
            throw new RuntimeException(TENANT_ID_HEADER + " is null");
        }
        try {
            TenantIdContext.setTenantId(Long.valueOf(tenantId));
            chain.doFilter(servletRequest, servletResponse);
        } finally {
            TenantIdContext.clear();
        }
    }
}