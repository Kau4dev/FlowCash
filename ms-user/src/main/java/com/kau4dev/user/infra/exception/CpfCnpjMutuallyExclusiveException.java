package com.kau4dev.user.infra.exception;

public class CpfCnpjMutuallyExclusiveException extends RuntimeException {
    public CpfCnpjMutuallyExclusiveException(String message) {
        super(message);
    }
}
