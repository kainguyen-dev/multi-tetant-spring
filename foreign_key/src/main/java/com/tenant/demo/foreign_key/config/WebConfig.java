package com.tenant.demo.foreign_key.config;

import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public TenantIdFilter tenantFilter() {
        return new TenantIdFilter();
    }

    @Bean
    public FilterRegistrationBean<TenantIdFilter> tenantFilterRegistration() {
        FilterRegistrationBean<TenantIdFilter> result = new FilterRegistrationBean<>();
        result.setFilter(this.tenantFilter());
        result.setUrlPatterns(List.of("/*"));
        result.setName("Tenant Store Filter");
        result.setOrder(1);
        return result;
    }

}
