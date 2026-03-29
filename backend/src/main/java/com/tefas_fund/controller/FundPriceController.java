package com.tefas_fund.controller;

import com.tefas_fund.service.FundPriceService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fund/price")
public class FundPriceController {
    private final FundPriceService fundPriceService;

    public FundPriceController(FundPriceService fundService) {
        this.fundPriceService = fundService;
    }

    @GetMapping()
    public void getPriceHistory(
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) Integer months
    ) {
        fundPriceService.getPriceHistory(days, months);
    }

    @GetMapping("/{fund}")
    public void getData(
            @PathVariable String fund,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) Integer months
    ) {
        fundPriceService.getData(fund, days, months);
    }

    @GetMapping("/daily")
    public void fetchFundData() {
        fundPriceService.getDailyPrice();
    }

    @GetMapping("/daily/{fund}")
    public void getDailyData(@PathVariable String fund) {
        fundPriceService.getDailyPriceBySymbol(fund);
    }
}
