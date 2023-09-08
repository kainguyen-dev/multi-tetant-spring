package com.tenant.demo.foreign_key.service;

import com.tenant.demo.foreign_key.domain.DataLog;
import com.tenant.demo.foreign_key.repository.DataLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DataLogService {

    private final DataLogRepository dataLogRepository;

    @Transactional
    public List<DataLog> getAll() {
        return dataLogRepository.findAll();
    }
}
