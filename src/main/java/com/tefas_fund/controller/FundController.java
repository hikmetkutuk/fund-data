package com.tefas_fund.controller;

import com.tefas_fund.dto.CalculateResponse;
import com.tefas_fund.service.FundService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fund")
public class FundController {
    private final FundService fundService;

    public FundController(FundService fundService) {
        this.fundService = fundService;
    }

    @GetMapping("/calculate")
    public List<CalculateResponse> calculate() {
        return fundService.calculate();
    }
}
