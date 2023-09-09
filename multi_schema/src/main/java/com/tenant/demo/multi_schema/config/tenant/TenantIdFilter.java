package com.tenant.demo.multi_schema.config.tenant;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.tenant.demo.multi_schema.config.tenant.TenantConstants.TENANT_ID_HEADER;

@Slf4j
@WebFilter(urlPatterns = "/*", dispatcherTypes = {DispatcherType.REQUEST})
public class TenantIdFilter extends OncePerRequestFilter {

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String tenantId = request.getHeader(TENANT_ID_HEADER);
        log.info("Set Tenant ID = {} to thread Local", tenantId);
        if (tenantId == null) {
            throw new RuntimeException(TENANT_ID_HEADER + " is null");
        }
        TenantIdContext.setTenantId(tenantId);
        filterChain.doFilter(request, response);
        TenantIdContext.clear();
    }
}