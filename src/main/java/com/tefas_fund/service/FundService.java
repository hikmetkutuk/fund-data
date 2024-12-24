package com.tefas_fund.service;

import com.tefas_fund.model.Fund;
import com.tefas_fund.model.FundPrice;
import com.tefas_fund.repository.FundListRepository;
import com.tefas_fund.repository.FundPriceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FundService {
    private final FundListRepository fundListRepository;
    private final FundPriceRepository fundPriceRepository;
    private final CurrencyService currencyService;

    public FundService(FundListRepository fundListRepository, FundPriceRepository fundPriceRepository, CurrencyService currencyService) {
        this.fundListRepository = fundListRepository;
        this.fundPriceRepository = fundPriceRepository;
        this.currencyService = currencyService;
    }

    public List<String> getAllFunds() {
        return fundListRepository.findAll().stream().map(Fund::getSymbol).toList();
    }

    public double calculateMonthlyGrowthInDollars(String symbol) {
        LocalDate today = LocalDate.now();
        LocalDate oneMonthAgo = today.minusMonths(1);

        FundPrice todayPrice = fundPriceRepository.findByFundSymbolAndDate(symbol, today)
                .orElseThrow(() -> new IllegalArgumentException("Bugünkü fon verisi bulunamadı."));

        FundPrice oneMonthAgoPrice = fundPriceRepository.findByFundSymbolAndClosestDate(symbol, oneMonthAgo)
                .orElseThrow(() -> new IllegalArgumentException("Bir ay önceye yakın fon verisi bulunamadı."));

        double currentFundValueInUsd = todayPrice.getPrice() / currencyService.getCurrencyPrice("USDTRY", today);
        double previousFundValueInUsd = oneMonthAgoPrice.getPrice() / currencyService.getCurrencyPrice("USDTRY", oneMonthAgo);

        if (previousFundValueInUsd == 0) {
            throw new IllegalArgumentException("Bir ay önceki dolar bazlı fon değeri sıfır olamaz.");
        }

        return ((currentFundValueInUsd - previousFundValueInUsd) / previousFundValueInUsd) * 100;
    }
}
