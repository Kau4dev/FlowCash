package com.kau4dev.transfer.infra.exception;

public class MerchantCannotTransferException extends RuntimeException {
    public MerchantCannotTransferException(String message) {
        super(message);
    }
}
