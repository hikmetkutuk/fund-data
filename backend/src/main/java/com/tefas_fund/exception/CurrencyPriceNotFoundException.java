package com.tefas_fund.exception;

import java.time.LocalDate;

public class CurrencyPriceNotFoundException extends RuntimeException {
    public CurrencyPriceNotFoundException(String currency, LocalDate date) {
        super("Currency price not found for " + currency + " on " + date + ".");
    }
}
