package com.tefas_fund.service;

import com.tefas_fund.dto.CalculateResponse;
import com.tefas_fund.model.Fund;
import com.tefas_fund.model.FundPrice;
import com.tefas_fund.repository.FundRepository;
import com.tefas_fund.repository.FundPriceRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FundService {
    private final FundRepository fundRepository;
    private final FundPriceRepository fundPriceRepository;
    private final CurrencyService currencyService;

    private static final LocalDate TODAY = LocalDate.now();

    public FundService(FundRepository fundRepository, FundPriceRepository fundPriceRepository, CurrencyService currencyService) {
        this.fundRepository = fundRepository;
        this.fundPriceRepository = fundPriceRepository;
        this.currencyService = currencyService;
    }

    public List<String> getAllFunds() {
        return fundRepository.findAll().stream().map(Fund::getSymbol).toList();
    }

    public List<CalculateResponse> calculate() {
        Map<String, Fund> fundMap = fundRepository.findAll().stream()
                .collect(Collectors.toMap(Fund::getSymbol, Function.identity()));

        Map<LocalDate, Double> currencyPrices = fetchCurrencyPrices();

        return fundMap.keySet().parallelStream()
                .map(symbol -> calculateResponseForFund(symbol, currencyPrices))
                .toList();
    }

    private Map<LocalDate, Double> fetchCurrencyPrices() {
        return Stream.of(
                        TODAY,
                        TODAY.minusMonths(1),
                        TODAY.minusMonths(3),
                        TODAY.minusMonths(6),
                        LocalDate.of(TODAY.getYear(), 1, 1),
                        TODAY.minusYears(1),
                        TODAY.minusYears(2),
                        TODAY.minusYears(3),
                        TODAY.minusYears(4),
                        TODAY.minusYears(5),
                        TODAY.minusYears(7),
                        TODAY.minusYears(10)
                )
                .distinct()
                .collect(Collectors.toMap(date -> date, date -> currencyService.getCurrencyPrice("USDTRY", date)));
    }

    private CalculateResponse calculateResponseForFund(String fund, Map<LocalDate, Double> currencyPrices) {
        Fund fundData = fundRepository.findBySymbol(fund);
        return new CalculateResponse(
                fund,
                fundData.getIndex(),
                calculateGrowth(fund, TODAY.minusMonths(1), currencyPrices),
                calculateGrowth(fund, TODAY.minusMonths(3), currencyPrices),
                calculateGrowth(fund, TODAY.minusMonths(6), currencyPrices),
                calculateGrowth(fund, LocalDate.of(TODAY.getYear(), 1, 1), currencyPrices),
                calculateGrowth(fund, TODAY.minusYears(1), currencyPrices),
                calculateGrowth(fund, TODAY.minusYears(2), currencyPrices),
                calculateGrowth(fund, TODAY.minusYears(3), currencyPrices),
                calculateGrowth(fund, TODAY.minusYears(4), currencyPrices),
                calculateGrowth(fund, TODAY.minusYears(5), currencyPrices),
                calculateGrowth(fund, TODAY.minusYears(7), currencyPrices),
                calculateGrowth(fund, TODAY.minusYears(10), currencyPrices)
        );
    }

    private Object calculateGrowth(String symbol, LocalDate previousDate, Map<LocalDate, Double> currencyPrices) {
        FundPrice todayPrice = fetchTodayFundPrice(symbol);
        Optional<FundPrice> previousFundPrice = fundPriceRepository.findByFundSymbolAndClosestDate(symbol, previousDate);

        double currentPriceInUsd = todayPrice.getPrice() / currencyPrices.get(TODAY);
        double previousPriceInUsd = previousFundPrice.map(price -> price.getPrice() / currencyPrices.get(previousDate))
                .orElse(0.0);

        if (previousPriceInUsd == 0) {
            return null;
        }

        return ((currentPriceInUsd - previousPriceInUsd) / previousPriceInUsd) * 100;
    }

    private FundPrice fetchTodayFundPrice(String symbol) {
        return fundPriceRepository.findByFundSymbolAndDate(symbol, TODAY)
                .orElseThrow(() -> new IllegalArgumentException("Fund price not found for today."));
    }
}
