package com.tenant.demo.multi_schema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;

@SpringBootApplication(exclude={LiquibaseAutoConfiguration.class})
public class MultiSchemaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiSchemaApplication.class, args);
    }

}
