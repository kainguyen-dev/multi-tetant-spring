package com.tenant.demo.multi_schema.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "TENANT")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;
}
