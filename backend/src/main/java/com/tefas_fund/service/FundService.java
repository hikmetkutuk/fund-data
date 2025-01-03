package com.tefas_fund.service;

import com.tefas_fund.dto.CalculateResponse;
import com.tefas_fund.model.Fund;
import com.tefas_fund.model.FundPrice;
import com.tefas_fund.model.Yield;
import com.tefas_fund.repository.YieldRepository;
import com.tefas_fund.repository.FundRepository;
import com.tefas_fund.repository.FundPriceRepository;
import com.tefas_fund.util.YieldSpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
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
    private final YieldRepository yieldRepository;

    private static final LocalDate TODAY = LocalDate.now();

    public FundService(FundRepository fundRepository, FundPriceRepository fundPriceRepository, CurrencyService currencyService, YieldRepository yieldRepository) {
        this.fundRepository = fundRepository;
        this.fundPriceRepository = fundPriceRepository;
        this.currencyService = currencyService;
        this.yieldRepository = yieldRepository;
    }

    public List<String> getAllFunds() {
        return fundRepository.findAll().stream().map(Fund::getSymbol).toList();
    }

    @Transactional
    public String updateYield() {
        try {
            List<CalculateResponse> newRecords = calculate();

            yieldRepository.deleteAll();

            List<Yield> entitiesToSave = newRecords.stream()
                    .map(this::mapToEntity)
                    .toList();
            yieldRepository.saveAll(entitiesToSave);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Successfully updated";
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
                        TODAY.minusYears(6),
                        TODAY.minusYears(7),
                        TODAY.minusYears(8),
                        TODAY.minusYears(9),
                        TODAY.minusYears(10)
                )
                .distinct()
                .collect(Collectors.toMap(date -> date, date -> currencyService.getCurrencyPrice("USDTRY", date)));
    }

    private CalculateResponse calculateResponseForFund(String fund, Map<LocalDate, Double> currencyPrices) {
        Fund fundData = fundRepository.findBySymbol(fund);
        CalculateResponse response = new CalculateResponse(
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
                calculateGrowth(fund, TODAY.minusYears(6), currencyPrices),
                calculateGrowth(fund, TODAY.minusYears(7), currencyPrices),
                calculateGrowth(fund, TODAY.minusYears(8), currencyPrices),
                calculateGrowth(fund, TODAY.minusYears(9), currencyPrices),
                calculateGrowth(fund, TODAY.minusYears(10), currencyPrices),
                0
        );
        double point = calculatePoints(response);

        return new CalculateResponse(
                response.symbol(),
                response.index(),
                response.oneMonthGrowth(),
                response.threeMonthGrowth(),
                response.sixMonthGrowth(),
                response.ytdGrowth(),
                response.oneYearGrowth(),
                response.twoYearGrowth(),
                response.threeYearGrowth(),
                response.fourYearGrowth(),
                response.fiveYearGrowth(),
                response.sixYearGrowth(),
                response.sevenYearGrowth(),
                response.eightYearGrowth(),
                response.nineYearGrowth(),
                response.tenYearGrowth(),
                point
        );
    }

    private double calculateGrowth(String symbol, LocalDate previousDate, Map<LocalDate, Double> currencyPrices) {
        FundPrice todayPrice = fetchTodayFundPrice(symbol);
        Optional<FundPrice> previousFundPrice = fundPriceRepository.findByFundSymbolAndClosestDate(symbol, previousDate);

        double currentPriceInUsd = todayPrice.getPrice() / currencyPrices.get(TODAY);
        double previousPriceInUsd = previousFundPrice.map(price -> price.getPrice() / currencyPrices.get(previousDate))
                .orElse(0.0);

        if (previousPriceInUsd == 0) {
            return 0;
        }

        double growth = ((currentPriceInUsd - previousPriceInUsd) / previousPriceInUsd) * 100;
        return Math.round(growth * 100.0) / 100.0;
    }

    private FundPrice fetchTodayFundPrice(String symbol) {
        return fundPriceRepository.findByFundSymbolAndDate(symbol, TODAY)
                .orElseThrow(() -> new IllegalArgumentException("Fund price not found for today."));
    }

    private Yield mapToEntity(CalculateResponse response) {
        return new Yield(
                response.symbol(),
                response.index(),
                response.oneMonthGrowth(),
                response.threeMonthGrowth(),
                response.sixMonthGrowth(),
                response.ytdGrowth(),
                response.oneYearGrowth(),
                response.twoYearGrowth(),
                response.threeYearGrowth(),
                response.fourYearGrowth(),
                response.fiveYearGrowth(),
                response.sixYearGrowth(),
                response.sevenYearGrowth(),
                response.eightYearGrowth(),
                response.nineYearGrowth(),
                response.tenYearGrowth(),
                response.point()
        );
    }

    public Page<Yield> getYield(String searchTerm, Pageable pageable) {
        YieldSpecification spec = new YieldSpecification(searchTerm);
        return yieldRepository.findAll(spec, pageable);
    }

    private double calculatePoints(CalculateResponse response) {
        Map<String, Double> weights = new HashMap<>();
        weights.put("1M", 0.14925);   // 1 month
        weights.put("3M", 0.44775);   // 3 months
        weights.put("6M", 0.8955);   // 6 months
        weights.put("YTD", 0.0);  // YTD
        weights.put("1Y", 1.7935);   // 1 year + 0,0025
        weights.put("2Y", 3.582);   // 2 years
        weights.put("3Y", 5.373);   // 3 years
        weights.put("4Y", 7.164);   // 4 years
        weights.put("5Y", 8.955);   // 5 years
        weights.put("6Y", 10.746);   // 6 years
        weights.put("7Y", 12.537);   // 7 years
        weights.put("8Y", 14.328);   // 8 years
        weights.put("9Y", 16.119);   // 9 years
        weights.put("10Y", 17.91);  // 10 years

        // Map periods to their growth values
        Map<String, Double> growthValues = new HashMap<>();
        growthValues.put("1M", response.oneMonthGrowth());
        growthValues.put("3M", response.threeMonthGrowth());
        growthValues.put("6M", response.sixMonthGrowth());
        growthValues.put("YTD", response.ytdGrowth());
        growthValues.put("1Y", response.oneYearGrowth());
        growthValues.put("2Y", response.twoYearGrowth());
        growthValues.put("3Y", response.threeYearGrowth());
        growthValues.put("4Y", response.fourYearGrowth());
        growthValues.put("5Y", response.fiveYearGrowth());
        growthValues.put("6Y", response.sixYearGrowth());
        growthValues.put("7Y", response.sevenYearGrowth());
        growthValues.put("8Y", response.eightYearGrowth());
        growthValues.put("9Y", response.nineYearGrowth());
        growthValues.put("10Y", response.tenYearGrowth());

        double availableWeightsSum = growthValues.entrySet().stream()
                .filter(entry -> entry.getValue() != 0.0)
                .mapToDouble(entry -> weights.get(entry.getKey()))
                .sum();

        double score = growthValues.entrySet().stream()
                .filter(entry -> entry.getValue() != 0.0)
                .mapToDouble(entry -> {
                    double normalizedWeight = weights.get(entry.getKey()) / availableWeightsSum;
                    return entry.getValue() * normalizedWeight;
                })
                .sum();

        return Math.round(score * 100.0) / 100.0;
    }
}
