package com.tenant.demo.multi_schema.service;

import com.tenant.demo.multi_schema.domain.entity.DataLog;
import com.tenant.demo.multi_schema.repository.tenants.DataLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DataLogService {
    private final DataLogRepository dataLogRepository;

    @Transactional(readOnly = true)
    public List<DataLog> findAll() {
        return dataLogRepository.findAll();
    }

    @Transactional
    public DataLog insert(DataLog dataLog) {
        return dataLogRepository.save(dataLog);
    }
}
