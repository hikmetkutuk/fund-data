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
import java.time.temporal.ChronoUnit;
import java.util.*;
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

        var records = fundMap.keySet().parallelStream()
                .map(symbol -> calculateResponseForFund(symbol, currencyPrices))
                .toList();

        return calculateScore(records);
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
                0
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
                response.score()
        );
    }

    public Page<Yield> getYield(String searchTerm, Pageable pageable) {
        YieldSpecification spec = new YieldSpecification(searchTerm);
        return yieldRepository.findAll(spec, pageable);
    }

    private List<CalculateResponse> calculateScore(List<CalculateResponse> records) {
        LocalDate startOfYear = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        LocalDate today = LocalDate.now();
        long ytdMonths = ChronoUnit.MONTHS.between(startOfYear, today);

        Map<String, Double> weights = new HashMap<>();
        weights.put("3M", 0.7);   // 3 months
        weights.put("6M", 0.75);   // 6 months
        if (ytdMonths > 6) {
            weights.put("YTD", 0.85);  // YTD
        }
        weights.put("1Y", 1.0);   // 1 year
        weights.put("2Y", 1.0);   // 2 years
        weights.put("3Y", 1.0);   // 3 years
        weights.put("4Y", 1.0);   // 4 years
        weights.put("5Y", 1.0);   // 5 years
        weights.put("6Y", 1.0);   // 6 years
        weights.put("7Y", 1.0);   // 7 years
        weights.put("8Y", 1.0);   // 8 years
        weights.put("9Y", 1.0);   // 9 years
        weights.put("10Y", 1.0);  // 10 years

        Map<String, Double> maxValues = calculateMaxValues(records);
        List<CalculateResponse> updatedRecords = new ArrayList<>();

        for (CalculateResponse record : records) {
            double numerator = 0.0;
            double denominator = 0.0;

            for (Map.Entry<String, Double> entry : weights.entrySet()) {
                String key = entry.getKey();
                Double weight = entry.getValue();
                Double value = getGrowthValue(record, key);

                if (value != null && value != 0.0 && weight > 0.0) {
                    numerator += value / maxValues.get(key) * weight;
                    denominator += weight;
                }
            }

            double score = (denominator == 0.0) ? 0.0 : (numerator / denominator) * 100;
            double roundedScore = Math.round(score * 100.0) / 100.0;

            CalculateResponse updatedRecord = new CalculateResponse(
                    record.symbol(),
                    record.index(),
                    record.oneMonthGrowth(),
                    record.threeMonthGrowth(),
                    record.sixMonthGrowth(),
                    record.ytdGrowth(),
                    record.oneYearGrowth(),
                    record.twoYearGrowth(),
                    record.threeYearGrowth(),
                    record.fourYearGrowth(),
                    record.fiveYearGrowth(),
                    record.sixYearGrowth(),
                    record.sevenYearGrowth(),
                    record.eightYearGrowth(),
                    record.nineYearGrowth(),
                    record.tenYearGrowth(),
                    roundedScore
            );
            updatedRecords.add(updatedRecord);
        }

        return updatedRecords;
    }

    private static Map<String, Double> calculateMaxValues(List<CalculateResponse> records) {
        Map<String, Double> maxValues = new HashMap<>();
        maxValues.put("1M", records.stream().mapToDouble(CalculateResponse::oneMonthGrowth).max().orElse(1.0));
        maxValues.put("3M", records.stream().mapToDouble(CalculateResponse::threeMonthGrowth).max().orElse(1.0));
        maxValues.put("6M", records.stream().mapToDouble(CalculateResponse::sixMonthGrowth).max().orElse(1.0));
        maxValues.put("YTD", records.stream().mapToDouble(CalculateResponse::ytdGrowth).max().orElse(1.0));
        maxValues.put("1Y", records.stream().mapToDouble(CalculateResponse::oneYearGrowth).max().orElse(1.0));
        maxValues.put("2Y", records.stream().mapToDouble(CalculateResponse::twoYearGrowth).max().orElse(1.0));
        maxValues.put("3Y", records.stream().mapToDouble(CalculateResponse::threeYearGrowth).max().orElse(1.0));
        maxValues.put("4Y", records.stream().mapToDouble(CalculateResponse::fourYearGrowth).max().orElse(1.0));
        maxValues.put("5Y", records.stream().mapToDouble(CalculateResponse::fiveYearGrowth).max().orElse(1.0));
        maxValues.put("6Y", records.stream().mapToDouble(CalculateResponse::sixYearGrowth).max().orElse(1.0));
        maxValues.put("7Y", records.stream().mapToDouble(CalculateResponse::sevenYearGrowth).max().orElse(1.0));
        maxValues.put("8Y", records.stream().mapToDouble(CalculateResponse::eightYearGrowth).max().orElse(1.0));
        maxValues.put("9Y", records.stream().mapToDouble(CalculateResponse::nineYearGrowth).max().orElse(1.0));
        maxValues.put("10Y", records.stream().mapToDouble(CalculateResponse::tenYearGrowth).max().orElse(1.0));
        return maxValues;
    }

    private static Double getGrowthValue(CalculateResponse record, String key) {
        return switch (key) {
            case "1M" -> record.oneMonthGrowth();
            case "3M" -> record.threeMonthGrowth();
            case "6M" -> record.sixMonthGrowth();
            case "YTD" -> record.ytdGrowth();
            case "1Y" -> record.oneYearGrowth();
            case "2Y" -> record.twoYearGrowth();
            case "3Y" -> record.threeYearGrowth();
            case "4Y" -> record.fourYearGrowth();
            case "5Y" -> record.fiveYearGrowth();
            case "6Y" -> record.sixYearGrowth();
            case "7Y" -> record.sevenYearGrowth();
            case "8Y" -> record.eightYearGrowth();
            case "9Y" -> record.nineYearGrowth();
            case "10Y" -> record.tenYearGrowth();
            default -> 0.0;
        };
    }
}
