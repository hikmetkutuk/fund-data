package com.tefas_fund.dto;

import java.time.LocalDate;

public record CurrencyRequest(
    String currency,
    LocalDate date,
    Double price
) {
}
