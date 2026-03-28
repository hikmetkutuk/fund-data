package com.tefas_fund.service;

import com.tefas_fund.config.ChromeDriverFactory;
import com.tefas_fund.model.FundPrice;
import com.tefas_fund.repository.FundPriceRepository;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
import java.util.stream.Collectors;

@Service
public class FundPriceService {
    private final FundPriceRepository fundPriceRepository;
    private final FundService fundService;
    private final ChromeDriverFactory chromeDriverFactory;

    public FundPriceService(FundPriceRepository fundPriceRepository, FundService fundService, ChromeDriverFactory chromeDriverFactory) {
        this.fundPriceRepository = fundPriceRepository;
        this.fundService = fundService;
        this.chromeDriverFactory = chromeDriverFactory;
    }

    public void getData(String fund) {
        WebDriver driver = chromeDriverFactory.createDriver();

        String url = "https://www.tefas.gov.tr/FonAnaliz.aspx?FonKod=" + fund;
        driver.get(url);

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='MainContent_RadioButtonListPeriod_1']")));

            WebElement radioButton = driver.findElement(By.id("MainContent_RadioButtonListPeriod_1"));
            radioButton.click();

            Thread.sleep(2000);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("MainContent_FonFiyatGrafik")));

            String jsScriptDates = "return chartMainContent_FonFiyatGrafik.xAxis[0].categories;";
            List<String> dateSeries = extractStringList(driver, jsScriptDates);

            String jsScriptPrices = "return chartMainContent_FonFiyatGrafik.series[0].data.map(dataPoint => dataPoint.y);";
            List<Double> priceData = extractDoubleList(driver, jsScriptPrices);

            List<Map<String, Object>> result = new ArrayList<>();
            for (int i = 0; i < dateSeries.size(); i++) {
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("date", dateSeries.get(i));
                dataPoint.put("price", priceData.get(i));
                result.add(dataPoint);
            }

            saveFundPriceData(fund, result);

        } catch (Exception e) {
            throw new RuntimeException("Error while scraping data for ticker: " + fund, e);
        } finally {
            driver.quit();
        }
    }

    public void saveFundPriceData(String symbol, List<Map<String, Object>> data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        fundPriceRepository.syncIdSequence();
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

            Optional<FundPrice> existingData = fundPriceRepository.findBySymbolAndDate(symbol, date);
            if (existingData.isPresent()) {
                System.out.println("Data already exists for symbol: " + symbol + " and date: " + date);
            } else {
                FundPrice fund = new FundPrice();
                fund.setSymbol(symbol);
                fund.setDate(date);
                fund.setPrice(price);

                fundPriceRepository.save(fund);
            }
        }
    }

    public void getPriceHistory() {
        var funds = fundService.getAllFunds();
        for (String fund : funds) {
            getData(fund);
        }
    }

    public void saveDailyPrice(String fund) {
        WebDriver driver = chromeDriverFactory.createDriver();
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            String url = "https://www.tefas.gov.tr/FonAnaliz.aspx?FonKod=" + fund;
            driver.get(url);
            Thread.sleep(2000);
            WebElement spanElement = driver.findElements(By.tagName("span")).get(3);
            String data = spanElement.getText();

            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            dataPoint.put("price", Double.parseDouble(data.replace(",", ".")));
            result.add(dataPoint);
            saveFundPriceData(fund, result);
        } catch (Exception e) {
            System.out.println("Fon Kodu: " + fund + ", Hata: Veri alınamadı. " + e.getMessage());
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

    private List<String> extractStringList(WebDriver driver, String script) {
        Object result = ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(script);
        if (!(result instanceof List<?> rawList)) {
            throw new IllegalStateException("Unexpected script result type for string list.");
        }

        return rawList.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    private List<Double> extractDoubleList(WebDriver driver, String script) {
        Object result = ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(script);
        if (!(result instanceof List<?> rawList)) {
            throw new IllegalStateException("Unexpected script result type for numeric list.");
        }

        return rawList.stream()
                .map(this::toDouble)
                .collect(Collectors.toList());
    }

    private double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        throw new IllegalStateException("Unexpected numeric value type: " + value);
    }
}
