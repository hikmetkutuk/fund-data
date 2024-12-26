package com.tefas_fund.dto;

import java.time.LocalDate;

public record CurrencyResponse(
        Long id,
        String currency,
        LocalDate date,
        Double price
) {
}
