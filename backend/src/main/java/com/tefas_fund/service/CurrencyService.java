package com.tefas_fund.service;

import com.tefas_fund.config.ChromeDriverFactory;
import com.tefas_fund.dto.CurrencyRequest;
import com.tefas_fund.exception.CurrencyOperationException;
import com.tefas_fund.exception.CurrencyPriceNotFoundException;
import com.tefas_fund.model.CurrencyPrice;
import com.tefas_fund.repository.CurrencyRepository;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class CurrencyService {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);

    private final CurrencyRepository currencyRepository;
    private final ChromeDriverFactory chromeDriverFactory;

    public CurrencyService(CurrencyRepository currencyRepository, ChromeDriverFactory chromeDriverFactory) {
        this.currencyRepository = currencyRepository;
        this.chromeDriverFactory = chromeDriverFactory;
    }

    @Transactional
    public Double getUsdTryPrice(boolean willBeRecorded) {
        WebDriver driver = chromeDriverFactory.createDriver();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        try {
            driver.get("https://www.doviz.com");
            WebElement usdTryElement = driver.findElement(By.cssSelector(
                    "span.value[data-socket-key='USD'][data-socket-attr='s']"
            ));
            String priceText = usdTryElement.getText().replace(",", ".");
            double price = Double.parseDouble(priceText);
            CurrencyRequest dailyUsdPrice = new CurrencyRequest(
                    "USDTRY",
                    LocalDate.parse(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), formatter),
                    price
            );
            saveUsdTryPrice(dailyUsdPrice);
            return price;
        } catch (WebDriverException | NumberFormatException e) {
            throw new CurrencyOperationException("USDTRY fiyatı alınamadı.", e);
        } finally {
            driver.quit();
        }
    }

    public void saveUsdTryPrice(CurrencyRequest currencyRequest) {
        try {
            boolean exists = currencyRepository.existsByCurrencyAndDate(currencyRequest.currency(), currencyRequest.date());

            if (!exists) {
                currencyRepository.syncIdSequence();
                var newCurrency = new CurrencyPrice();
                newCurrency.setCurrency(currencyRequest.currency());
                newCurrency.setDate(currencyRequest.date());
                newCurrency.setPrice(currencyRequest.price());
                currencyRepository.save(newCurrency);
            }
        } catch (DataIntegrityViolationException e) {
            throw new CurrencyOperationException("Kayıt zaten mevcut.", e);
        } catch (Exception e) {
            throw new CurrencyOperationException("USDTRY kaydı kaydedilemedi.", e);
        }
    }

    public void csvReader() {
        String filePath = "src/main/resources/usd.csv";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        currencyRepository.syncIdSequence();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String header = br.readLine();
            if (header == null) {
                logger.warn("USD CSV file is empty: {}", filePath);
                return;
            }

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length == 4) {
                    String csvId = values[0];
                    String currencyCode = values[1];
                    LocalDate date = LocalDate.parse(values[2], formatter);
                    Double value = Double.parseDouble(values[3]);

                    if (!currencyRepository.existsByCurrencyAndDate(currencyCode, date)) {
                        CurrencyPrice newCurrency = new CurrencyPrice();
                        newCurrency.setCurrency(currencyCode);
                        newCurrency.setDate(date);
                        newCurrency.setPrice(value);
                        currencyRepository.save(newCurrency);
                        logger.debug("Imported currency row {} for {} on {}", csvId, currencyCode, date);
                    }
                }
            }
        } catch (IOException e) {
            throw new CurrencyOperationException("USD CSV dosyası okunamadı.", e);
        }
    }


    public Double getCurrencyPrice(String currency, LocalDate date) {
        return currencyRepository.findByCurrencyAndDate(currency, date)
                .map(CurrencyPrice::getPrice)
                .orElseThrow(() -> new CurrencyPriceNotFoundException(currency, date));
    }
}
