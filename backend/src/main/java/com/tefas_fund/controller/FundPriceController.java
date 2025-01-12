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
    public void getPriceHistory() {
        fundPriceService.getPriceHistory();
    }

    @GetMapping("/{fund}")
    public void getData(@PathVariable String fund) {
        fundPriceService.getData(fund);
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
