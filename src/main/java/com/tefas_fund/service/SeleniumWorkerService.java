package com.tefas_fund.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeleniumWorkerService {
    public List<Map<String, Object>> getData(String fund) {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu");
        WebDriver driver = new ChromeDriver(options);

        String url = "https://www.tefas.gov.tr/FonAnaliz.aspx?FonKod=" + fund;
        driver.get(url);

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='MainContent_RadioButtonListPeriod_7']")));

            WebElement radioButton = driver.findElement(By.id("MainContent_RadioButtonListPeriod_7"));
            radioButton.click();

            Thread.sleep(2000);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("MainContent_FonFiyatGrafik")));

            String jsScriptDates = "return chartMainContent_FonFiyatGrafik.xAxis[0].categories;";
            List<String> dateSeries = (List<String>) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(jsScriptDates);

            String jsScriptPrices = "return chartMainContent_FonFiyatGrafik.series[0].data.map(dataPoint => dataPoint.y);";
            List<Double> priceData = (List<Double>) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(jsScriptPrices);

            List<Map<String, Object>> result = new ArrayList<>();
            for (int i = 0; i < dateSeries.size(); i++) {
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("date", dateSeries.get(i));
                dataPoint.put("price", priceData.get(i));
                result.add(dataPoint);
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error while scraping data for ticker: " + fund, e);
        } finally {
            driver.quit();
        }
    }
}
