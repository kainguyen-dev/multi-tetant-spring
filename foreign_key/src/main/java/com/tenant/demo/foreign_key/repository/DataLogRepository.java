package com.tenant.demo.foreign_key.repository;

import com.tenant.demo.foreign_key.domain.DataLog;
import com.tenant.demo.foreign_key.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface DataLogRepository extends JpaRepository<DataLog, Long> {
}
