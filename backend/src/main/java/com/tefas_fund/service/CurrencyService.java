package com.tefas_fund.service;

import com.tefas_fund.config.ChromeDriverFactory;
import com.tefas_fund.dto.CurrencyRequest;
import com.tefas_fund.dto.CurrencyResponse;
import com.tefas_fund.model.CurrencyPrice;
import com.tefas_fund.repository.CurrencyRepository;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CurrencyService {
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
        CurrencyRequest dailyUsdPrice = null;
        try {
            driver.get("https://www.doviz.com");
            WebElement usdTryElement = driver.findElement(By.cssSelector(
                    "span.value[data-socket-key='USD'][data-socket-attr='s']"
            ));
            String priceText = usdTryElement.getText().replace(",", ".");
            double price = Double.parseDouble(priceText);
            dailyUsdPrice = new CurrencyRequest(
                    "USDTRY",
                    LocalDate.parse(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), formatter),
                    price
            );
            saveUsdTryPrice(dailyUsdPrice);
            return price;
        } catch (Exception e) {
            throw new RuntimeException("Beklenmeyen hata oluştu: " + e.getMessage());
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
            throw new RuntimeException("Kayıt zaten mevcut: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Beklenmeyen hata: " + e.getMessage());
        }
    }

    public void csvReader() {
        String filePath = "src/main/resources/usd.csv";
        List<CurrencyResponse> records = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        currencyRepository.syncIdSequence();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length == 4) {
                    Long id = Long.parseLong(values[0]);
                    String name = values[1];
                    LocalDate date = LocalDate.parse(values[2], formatter);
                    Double value = Double.parseDouble(values[3]);
                    CurrencyResponse record = new CurrencyResponse(id, name, date, value);
                    records.add(record);

                    if (!currencyRepository.existsByCurrencyAndDate(name, date)) {
                        CurrencyPrice newCurrency = new CurrencyPrice();
                        newCurrency.setCurrency(name);
                        newCurrency.setDate(date);
                        newCurrency.setPrice(value);
                        currencyRepository.save(newCurrency);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (CurrencyResponse record : records) {
            System.out.println(record);
        }
    }


    public Double getCurrencyPrice(String currency, LocalDate date) {
        return currencyRepository.findByCurrencyAndDate(currency, date).get().getPrice();
    }
}
