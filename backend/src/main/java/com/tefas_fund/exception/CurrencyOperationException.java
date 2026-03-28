package com.tefas_fund.exception;

public class CurrencyOperationException extends RuntimeException {
    public CurrencyOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
