package com.tenant.demo.multi_schema.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;

import java.io.Serializable;

@Entity
@Table(name = "data_log")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DataLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_value")
    private String dataValue;
}
