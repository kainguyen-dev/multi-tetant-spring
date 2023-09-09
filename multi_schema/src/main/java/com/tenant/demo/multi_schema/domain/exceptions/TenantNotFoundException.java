package com.tenant.demo.multi_schema.domain.exceptions;

public class TenantNotFoundException extends Exception {
    public TenantNotFoundException(String message) {
        super(message);
    }
}
