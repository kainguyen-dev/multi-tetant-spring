package com.tenant.demo.foreign_key.controller;

import com.tenant.demo.foreign_key.domain.DataLog;
import com.tenant.demo.foreign_key.service.DataLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DataLogController {
    private final DataLogService dataLogService;

    @GetMapping("/data_log")
    public List<DataLog> getAll() {
        return dataLogService.getAll();
    }
}
