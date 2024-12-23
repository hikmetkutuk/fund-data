package com.tefas_fund.controller;

import com.tefas_fund.service.CurrencyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {
    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping("/usdtry")
    public String getUsdTryRate() {
        return currencyService.getUsdTryRate();
    }

    @GetMapping("/csv")
    public void csvReader() {
        currencyService.csvReader();
    }
}