package com.tefas_fund.service;

import com.tefas_fund.dto.CurrencyResponse;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CurrencyService {
    public String getUsdTryRate() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu");
        WebDriver driver = new ChromeDriver(options);
        String price = "";

        try {
            driver.get("https://www.doviz.com");
            WebElement usdTryElement = driver.findElement(By.cssSelector(
                    "span.value[data-socket-key='USD'][data-socket-attr='s']"
            ));
            price = usdTryElement.getText();
        } catch (Exception e) {
            System.out.println("Hata: Veri alınamadı. " + e.getMessage());
        } finally {
            driver.quit();
        }

        return price;
    }

    public void csvReader() {
        String filePath = "src/main/resources/usd.csv";
        List<CurrencyResponse> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length == 4) {
                    Long id = Long.parseLong(values[0]);
                    String name = values[1];
                    LocalDate date = LocalDate.parse(values[2]);
                    Double value = Double.parseDouble(values[3]);
                    CurrencyResponse record = new CurrencyResponse(id, name, date, value);
                    records.add(record);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Record record : records) {
            System.out.println(record);
        }
    }
}
