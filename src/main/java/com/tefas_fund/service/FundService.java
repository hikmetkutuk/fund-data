package com.tefas_fund.service;

import com.tefas_fund.model.FundPrice;
import com.tefas_fund.repository.FundRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.format.DateTimeFormatter;

@Service
public class FundService {
    private final FundRepository fundRepository;

    public FundService(FundRepository fundRepository) {
        this.fundRepository = fundRepository;
    }

    public void saveFundData(String symbol, List<Map<String, Object>> data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        for (Map<String, Object> entry : data) {
            String dateStr = (String) entry.get("date");
            Object priceObject = entry.get("price");
            double price = 0.0;

            if (priceObject instanceof Long) {
                price = ((Long) priceObject).doubleValue();
            } else if (priceObject instanceof Double) {
                price = (Double) priceObject;
            }

            LocalDate date = LocalDate.parse(dateStr, formatter);

            Optional<FundPrice> existingData = fundRepository.findBySymbolAndDate(symbol, date);
            if (existingData.isPresent()) {
                System.out.println("Data already exists for symbol: " + symbol + " and date: " + date);
            } else {
                FundPrice fund = new FundPrice();
                fund.setSymbol(symbol);
                fund.setDate(date);
                fund.setPrice(price);

                fundRepository.save(fund);
            }
        }
    }
}
