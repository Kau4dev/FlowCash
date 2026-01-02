package com.kau4dev.user.infra.exception;

public class CpfCnpjRequiredException extends RuntimeException {
    public CpfCnpjRequiredException(String message) {
        super(message);
    }
}
