package com.tenant.demo.multi_schema.controller;

import com.tenant.demo.multi_schema.domain.entity.DataLog;
import com.tenant.demo.multi_schema.service.DataLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DataLogController {
    private final DataLogService dataLogService;

    @GetMapping("/data_log")
    public List<DataLog> getAll() {
        return dataLogService.findAll();
    }

    @PostMapping("/data_log")
    public DataLog insert(@RequestBody DataLog dataLog) {
        return dataLogService.insert(dataLog);
    }
}
