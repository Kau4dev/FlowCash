package com.kau4dev.wallet.infra.exception;

public class InvalidDepositValueException extends RuntimeException {
    public InvalidDepositValueException(String message) {
        super(message);
    }
}
