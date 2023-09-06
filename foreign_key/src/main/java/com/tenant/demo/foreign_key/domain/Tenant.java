package com.tenant.demo.foreign_key.domain;

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
