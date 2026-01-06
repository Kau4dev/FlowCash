package com.kau4dev.user.infra.exception;

public class CpfCnpjAlreadyExistsException extends RuntimeException {
    public CpfCnpjAlreadyExistsException(String message) {
        super(message);
    }

}
