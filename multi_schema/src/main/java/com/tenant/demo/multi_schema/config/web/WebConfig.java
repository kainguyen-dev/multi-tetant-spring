package com.tenant.demo.multi_schema.config.web;

import com.tenant.demo.multi_schema.config.tenant.TenantIdFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
public class WebConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).addFilterAfter(new TenantIdFilter(), AnonymousAuthenticationFilter.class);
        return http.build();
    }

}
