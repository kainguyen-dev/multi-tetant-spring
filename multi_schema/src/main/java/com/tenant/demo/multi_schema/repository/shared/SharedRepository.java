package com.tenant.demo.multi_schema.repository.shared;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface SharedRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
}
