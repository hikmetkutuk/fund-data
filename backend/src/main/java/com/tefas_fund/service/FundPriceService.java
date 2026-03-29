package com.tefas_fund.service;

import com.tefas_fund.config.ChromeDriverFactory;
import com.tefas_fund.exception.FundPriceOperationException;
import com.tefas_fund.model.FundPrice;
import com.tefas_fund.repository.FundPriceRepository;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FundPriceService {
    private static final Logger logger = LoggerFactory.getLogger(FundPriceService.class);
    private static final String DATE_KEY = "date";
    private static final String PRICE_KEY = "price";
    private static final String TEFAS_FUND_ANALYSIS_URL = "https://www.tefas.gov.tr/FonAnaliz.aspx?FonKod=";
    private static final DateTimeFormatter TEFAS_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String PRICE_CHART_ID = "MainContent_FonFiyatGrafik";
    private static final Duration SCRAPE_WAIT_TIMEOUT = Duration.ofSeconds(30);
    private static final String CHART_READY_SCRIPT = """
            return typeof chartMainContent_FonFiyatGrafik !== 'undefined'
                && chartMainContent_FonFiyatGrafik.xAxis
                && chartMainContent_FonFiyatGrafik.xAxis[0]
                && chartMainContent_FonFiyatGrafik.xAxis[0].categories
                && chartMainContent_FonFiyatGrafik.xAxis[0].categories.length > 0
                && chartMainContent_FonFiyatGrafik.series
                && chartMainContent_FonFiyatGrafik.series[0]
                && chartMainContent_FonFiyatGrafik.series[0].data
                && chartMainContent_FonFiyatGrafik.series[0].data.length > 0;
            """;

    private final FundPriceRepository fundPriceRepository;
    private final FundService fundService;
    private final ChromeDriverFactory chromeDriverFactory;

    public FundPriceService(FundPriceRepository fundPriceRepository, FundService fundService, ChromeDriverFactory chromeDriverFactory) {
        this.fundPriceRepository = fundPriceRepository;
        this.fundService = fundService;
        this.chromeDriverFactory = chromeDriverFactory;
    }

    public void getData(String fund) {
        getData(fund, null, null);
    }

    public void getData(String fund, Integer days, Integer months) {
        WebDriver driver = chromeDriverFactory.createDriver();
        try {
            driver.get(TEFAS_FUND_ANALYSIS_URL + fund);
            waitForPageReady(driver);
            selectRequestedPeriod(driver, days, months);
            waitForChartData(driver);
            List<Map<String, Object>> result = extractPriceSeries(driver, resolveCutoffDate(days, months));
            if (result.isEmpty()) {
                logger.warn("No price data found for fund {} with days={} months={}", fund, days, months);
                return;
            }
            saveFundPriceData(fund, result);
        } catch (Exception e) {
            throw new FundPriceOperationException("Error while scraping data for ticker: " + fund, e);
        } finally {
            driver.quit();
        }
    }

    public void saveFundPriceData(String symbol, List<Map<String, Object>> data) {
        fundPriceRepository.syncIdSequence();
        for (Map<String, Object> entry : data) {
            String dateStr = (String) entry.get(DATE_KEY);
            Object priceObject = entry.get(PRICE_KEY);
            double price = toDouble(priceObject);

            LocalDate date = LocalDate.parse(dateStr, TEFAS_DATE_FORMATTER);

            Optional<FundPrice> existingData = fundPriceRepository.findBySymbolAndDate(symbol, date);
            if (existingData.isPresent()) {
                logger.debug("Data already exists for symbol: {} and date: {}", symbol, date);
            } else {
                FundPrice fundPrice = new FundPrice();
                fundPrice.setSymbol(symbol);
                fundPrice.setDate(date);
                fundPrice.setPrice(price);

                fundPriceRepository.save(fundPrice);
            }
        }
    }

    public void getPriceHistory() {
        getPriceHistory(null, null);
    }

    public void getPriceHistory(Integer days, Integer months) {
        var funds = fundService.getAllFunds();
        for (String fund : funds) {
            try {
                getData(fund, days, months);
            } catch (FundPriceOperationException exception) {
                logger.warn("Skipping fund {} after scraper failure", fund, exception);
            }
        }
    }

    public void saveDailyPrice(String fund) {
        WebDriver driver = chromeDriverFactory.createDriver();
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            String url = TEFAS_FUND_ANALYSIS_URL + fund;
            driver.get(url);
            Thread.sleep(2000);
            WebElement spanElement = driver.findElements(By.tagName("span")).get(3);
            String data = spanElement.getText();

            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put(DATE_KEY, LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            dataPoint.put(PRICE_KEY, Double.parseDouble(data.replace(",", ".")));
            result.add(dataPoint);
            saveFundPriceData(fund, result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Price fetch interrupted for fund {}", fund, e);
        } catch (Exception e) {
            logger.warn("Fon Kodu: {}, Hata: Veri alınamadı.", fund, e);
        } finally {
            driver.quit();
        }

    }

    public void getDailyPrice() {
        var funds = fundService.getAllFunds();

        for (String fund : funds) {
            saveDailyPrice(fund);
        }
    }

    public void getDailyPriceBySymbol(String fund) {
        saveDailyPrice(fund);
    }

    private double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        throw new IllegalStateException("Unexpected numeric value type: " + value);
    }

    private void waitForPageReady(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, SCRAPE_WAIT_TIMEOUT);
        wait.until(ignored -> "complete".equals(((JavascriptExecutor) driver).executeScript("return document.readyState")));
        String pageSource = Optional.ofNullable(driver.getPageSource()).orElse("");
        if (pageSource.contains("Request Rejected")) {
            throw new IllegalStateException("TEFAS rejected the Selenium request.");
        }
    }

    private void selectRequestedPeriod(WebDriver driver, Integer days, Integer months) {
        String periodButtonId = resolvePeriodButtonId(days, months);
        if (periodButtonId == null) {
            return;
        }

        WebDriverWait wait = new WebDriverWait(driver, SCRAPE_WAIT_TIMEOUT);
        WebElement button = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(periodButtonId)));
        if (button.isSelected()) {
            return;
        }

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
        wait.until(ignored -> {
            try {
                return driver.findElement(By.id(periodButtonId)).isSelected();
            } catch (Exception exception) {
                return false;
            }
        });
        waitForPageReady(driver);
    }

    private void waitForChartData(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, SCRAPE_WAIT_TIMEOUT);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(PRICE_CHART_ID)));
        wait.until(ignored -> {
            try {
                Object ready = ((JavascriptExecutor) driver).executeScript(CHART_READY_SCRIPT);
                return Boolean.TRUE.equals(ready);
            } catch (Exception exception) {
                return false;
            }
        });
    }

    private List<String> extractStringList(WebDriver driver, String script) {
        Object result = ((JavascriptExecutor) driver).executeScript(script);
        if (!(result instanceof List<?> rawList)) {
            throw new IllegalStateException("Unexpected script result type for string list.");
        }
        return rawList.stream().map(String::valueOf).toList();
    }

    private List<Double> extractDoubleList(WebDriver driver, String script) {
        Object result = ((JavascriptExecutor) driver).executeScript(script);
        if (!(result instanceof List<?> rawList)) {
            throw new IllegalStateException("Unexpected script result type for numeric list.");
        }
        return rawList.stream().map(this::toDouble).toList();
    }

    private List<Map<String, Object>> extractPriceSeries(WebDriver driver, LocalDate cutoffDate) {
        List<String> dateSeries = extractStringList(driver, "return chartMainContent_FonFiyatGrafik.xAxis[0].categories;");
        List<Double> priceData = extractDoubleList(driver,
                "return chartMainContent_FonFiyatGrafik.series[0].data.map(dataPoint => (dataPoint && typeof dataPoint.y !== 'undefined') ? dataPoint.y : dataPoint);");

        if (dateSeries.size() != priceData.size()) {
            throw new IllegalStateException("Price chart returned mismatched date and price counts.");
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < dateSeries.size(); i++) {
            LocalDate date = LocalDate.parse(dateSeries.get(i), TEFAS_DATE_FORMATTER);
            if (cutoffDate != null && date.isBefore(cutoffDate)) {
                continue;
            }

            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put(DATE_KEY, dateSeries.get(i));
            dataPoint.put(PRICE_KEY, priceData.get(i));
            result.add(dataPoint);
        }
        return result;
    }

    private String resolvePeriodButtonId(Integer days, Integer months) {
        if (days != null && months != null) {
            throw new IllegalArgumentException("Use either days or months, not both.");
        }
        if (days != null) {
            if (days <= 0) {
                throw new IllegalArgumentException("days must be greater than zero.");
            }
            if (days <= 7) {
                return "MainContent_RadioButtonListPeriod_0";
            }
            return null;
        }
        if (months == null) {
            return null;
        }
        return switch (months) {
            case 1 -> "MainContent_RadioButtonListPeriod_1";
            case 3 -> "MainContent_RadioButtonListPeriod_2";
            case 6 -> "MainContent_RadioButtonListPeriod_3";
            case 12 -> "MainContent_RadioButtonListPeriod_5";
            case 36 -> "MainContent_RadioButtonListPeriod_6";
            case 60 -> "MainContent_RadioButtonListPeriod_7";
            default -> null;
        };
    }

    private LocalDate resolveCutoffDate(Integer days, Integer months) {
        if (days != null && months != null) {
            throw new IllegalArgumentException("Use either days or months, not both.");
        }
        if (days != null) {
            if (days <= 0) {
                throw new IllegalArgumentException("days must be greater than zero.");
            }
            return LocalDate.now().minusDays(days);
        }
        if (months != null) {
            if (months <= 0) {
                throw new IllegalArgumentException("months must be greater than zero.");
            }
            return LocalDate.now().minusMonths(months);
        }
        return null;
    }
}
