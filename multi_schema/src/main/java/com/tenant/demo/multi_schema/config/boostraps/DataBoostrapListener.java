package com.tenant.demo.multi_schema.config.boostraps;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DataBoostrapListener implements ApplicationListener<ContextRefreshedEvent> {
    private final ResourceLoader resourceLoader;
    private final Map<String, DataSource> tenantDataSources;

    public DataBoostrapListener(ResourceLoader resourceLoader, @Qualifier("tenantDataSources") Map<String, DataSource> tenantDataSources) {
        this.resourceLoader = resourceLoader;
        this.tenantDataSources = tenantDataSources;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Start to load fake data at {}", event.getTimestamp());
        Resource resource = resourceLoader.getResource("classpath:fake_data/config.json");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            BoostrapItem[] config = objectMapper.readValue(resource.getInputStream(), BoostrapItem[].class);
            for (BoostrapItem item : config) {
                Set<String> dataValues = extractDataValues(item);
                DataSource dataSource = tenantDataSources.get(item.getSchema());
                if (dataSource == null) {
                    log.warn("Datasource of schema {} don't existed", item.getSchema());
                    continue;
                }
                insertDataLog(dataSource, dataValues);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> extractDataValues(BoostrapItem item) {
        try {
            Resource rawData = resourceLoader.getResource("classpath:" + item.getFilePath());
            Set<String> dataValues;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(rawData.getInputStream()))) {
                dataValues = reader.lines().collect(Collectors.toSet());
            }
            return dataValues;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertDataLog(DataSource dataSource, Set<String> dataValues) {
        try (Connection connection = dataSource.getConnection()) {
            String insertQuery = "INSERT INTO data_log(data_value) VALUES (?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            for (String dataValue : dataValues) {
                preparedStatement.setString(1, dataValue);
                preparedStatement.addBatch();
            }
            int[] batchResult = preparedStatement.executeBatch();
            log.info("Inserted " + batchResult.length + " rows into schema {}", connection.getSchema());
            connection.commit();
        } catch (SQLException e) {
            log.error("Error when import ", e);
        }
    }

    @Data
    @NoArgsConstructor
    static class BoostrapItem {
        private String schema;
        private String filePath;
        private boolean completed;
    }

}