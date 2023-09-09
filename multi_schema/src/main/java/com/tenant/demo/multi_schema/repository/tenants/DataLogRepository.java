package com.tenant.demo.multi_schema.repository.tenants;
import com.tenant.demo.multi_schema.domain.entity.DataLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataLogRepository extends JpaRepository<DataLog, Long> {
}
