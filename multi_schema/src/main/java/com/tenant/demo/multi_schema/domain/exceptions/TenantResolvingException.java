package com.tenant.demo.multi_schema.domain.exceptions;

public class TenantResolvingException extends Exception {
    public TenantResolvingException(Throwable throwable, String message) {
        super(message, throwable);
    }
}
