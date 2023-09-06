package com.tenant.demo.foreign_key.repository;

import com.tenant.demo.foreign_key.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
}
