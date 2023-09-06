package com.tenant.demo.foreign_key.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;

import java.io.Serializable;

@Entity
@Table(name = "DATA_LOG")
@FilterDef(name = "TENANT_FILTER", parameters = {@ParamDef(name = "tenantId", type = Long.class)})
@Filters({@Filter(name = "TENANT_FILTER", condition = "tenant_id = :tenantId")})
@NoArgsConstructor
@Data
public class DataLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_value")
    private String dataValue;

    @Column(name = "tenant_id")
    private Long tenantId;
}
