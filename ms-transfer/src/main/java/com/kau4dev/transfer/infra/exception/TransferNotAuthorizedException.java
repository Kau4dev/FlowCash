package com.kau4dev.transfer.infra.exception;

public class TransferNotAuthorizedException extends RuntimeException {
    public TransferNotAuthorizedException(String message) {
        super(message);
    }
}
