package com.tefas_fund.controller;

import com.tefas_fund.service.SeleniumWorkerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fund")
public class FundController {
    private final SeleniumWorkerService seleniumWorkerService;

    public FundController(SeleniumWorkerService seleniumWorkerService) {
        this.seleniumWorkerService = seleniumWorkerService;
    }

    @GetMapping("/process")
    public Map<String, List<Map<String, Object>>> getData(@RequestParam List<String> funds) {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();

        for (String fund : funds) {
            List<Map<String, Object>> data = seleniumWorkerService.getData(fund);
            result.put(fund, data);
        }

        return result;
    }
}
