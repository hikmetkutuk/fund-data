package com.tefas_fund.tools;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class NewFundsFiveYearBackfill {
    private static final Logger LOGGER = Logger.getLogger(NewFundsFiveYearBackfill.class.getName());
    private static final String[] SYMBOLS = {
            "AOY","KHB","GKG","GRT","ZSF","KH1","BIY","AU1","BLT","MTD","BTK","BCK","EVM","DHT","DRT","DTM","DTH","FAK","ITC","TIL","NAK","NKP","NKM","NTI","NKT","FAL","PEA","PTN","PHK","PIR","SKO",
            "KCL","NSP","TLK","PPG","RCV","CVL","BCO","VRK","EPA","GPN","GOP","PRR","KPI","KUA","KDE","KUD","KIK","KME","MPE","FTL","RRP","PKR","TLV",
            "EML","EPI","NST","NLE",
            "TVE","ZTG",
            "IYB","MBL","EDU","YSU","YAK","OBP","ZPC","AGC","HOY","FID","TPV","SFS","TMU","DGF","DNF","BAG","BUB","BFS","BVD","BRF","DID","KRR","TND","NBO","RGD","RKC","FSU","CKL","EPK",
            "GBG","GBC","AFV",
            "THF","OMG","AEV","IMB","SSS","HNC","GTH","HFR","HVI","SUR","NKC","NUH","SRL","SLG","KMN"
    };
    private static final String BASE_URL = "https://www.tefas.gov.tr/FonAnaliz.aspx?FonKod=";
    private static final String FIVE_YEAR_BUTTON_ID = "MainContent_RadioButtonListPeriod_7";
    private static final String CHART_DATES_SCRIPT = "return chartMainContent_FonFiyatGrafik.xAxis[0].categories;";
    private static final String CHART_PRICES_SCRIPT = "return chartMainContent_FonFiyatGrafik.series[0].data.map(dataPoint => (dataPoint && typeof dataPoint.y !== 'undefined') ? dataPoint.y : dataPoint);";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(30);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final LocalDate CUTOFF_DATE = LocalDate.now().minusYears(5);
    private static final int MAX_ATTEMPTS = 3;
    private static final int PROGRESS_LOG_INTERVAL = 10;
    private static final int CHART_REFRESH_WAIT_MILLIS = 3000;

    private NewFundsFiveYearBackfill() {
        // Utility entry point only.
    }

    public static void main(String[] args) throws IOException {
        Path csvPath = Path.of("/tmp/new_funds_5y_selenium.csv");
        Path failuresPath = Path.of("/tmp/new_funds_5y_selenium_failures.txt");
        List<String> csvLines = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        csvLines.add("symbol,date,price");

        processSymbols(csvLines, failures);
        Files.write(csvPath, csvLines);
        writeFailures(failuresPath, failures);
        logSummary(csvPath, failuresPath, csvLines.size() - 1, failures.size());
    }

    private static void processSymbols(List<String> csvLines, List<String> failures) {
        for (int index = 0; index < SYMBOLS.length; index++) {
            String symbol = SYMBOLS[index];
            List<String> rows = tryFetchRows(symbol, failures);
            csvLines.addAll(rows);
            logProgress(index + 1, csvLines.size() - 1, failures.size());
        }
    }

    private static List<String> tryFetchRows(String symbol, List<String> failures) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return fetchRowsForSymbol(symbol);
            } catch (Exception exception) {
                if (attempt == MAX_ATTEMPTS) {
                    failures.add(symbol + "\t" + exception.getClass().getSimpleName() + ": " + exception.getMessage());
                }
            }
        }
        return List.of();
    }

    private static List<String> fetchRowsForSymbol(String symbol) {
        WebDriver driver = createDriver();
        try {
            return fetchFiveYearRows(driver, symbol);
        } finally {
            driver.quit();
        }
    }

    private static void logProgress(int processedCount, int rowCount, int failureCount) {
        if (processedCount % PROGRESS_LOG_INTERVAL != 0 && processedCount != SYMBOLS.length) {
            return;
        }
        LOGGER.info(() -> "processed=" + processedCount + "/" + SYMBOLS.length
                + " rows=" + rowCount
                + " failures=" + failureCount);
    }

    private static void writeFailures(Path failuresPath, List<String> failures) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(failuresPath)) {
            for (String failure : failures) {
                writer.write(failure);
                writer.newLine();
            }
        }
    }

    private static void logSummary(Path csvPath, Path failuresPath, int totalRows, int totalFailures) {
        LOGGER.info(() -> "csv=" + csvPath);
        LOGGER.info(() -> "failures=" + failuresPath);
        LOGGER.info(() -> "total_rows=" + totalRows);
        LOGGER.info(() -> "total_failures=" + totalFailures);
    }

    private static WebDriver createDriver() {
        System.clearProperty("webdriver.chrome.driver");

        ChromeOptions options = new ChromeOptions();
        options.setBinary("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome");
        options.addArguments(
                "--headless=new",
                "--disable-gpu",
                "--window-size=1920,1080",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-blink-features=AutomationControlled",
                "--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36"
        );
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        return new ChromeDriver(options);
    }

    private static List<String> fetchFiveYearRows(WebDriver driver, String symbol) {
        driver.get(BASE_URL + symbol);

        WebDriverWait wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        wait.until(ignored -> "complete".equals(((JavascriptExecutor) driver).executeScript("return document.readyState")));

        String pageSource = String.valueOf(driver.getPageSource());
        if (pageSource.contains("Request Rejected")) {
            throw new IllegalStateException("TEFAS rejected browser session");
        }

        WebElement fiveYearButton = wait.until(ExpectedConditions.elementToBeClickable(By.id(FIVE_YEAR_BUTTON_ID)));
        fiveYearButton.click();

        wait.until(ignored -> driver.findElement(By.id(FIVE_YEAR_BUTTON_ID)).isSelected());
        wait.until(ignored -> "complete".equals(((JavascriptExecutor) driver).executeScript("return document.readyState")));
        waitForChartRefresh();

        List<?> rawDates = (List<?>) ((JavascriptExecutor) driver).executeScript(CHART_DATES_SCRIPT);
        List<?> rawPrices = (List<?>) ((JavascriptExecutor) driver).executeScript(CHART_PRICES_SCRIPT);

        if (rawDates.size() != rawPrices.size()) {
            throw new IllegalStateException("Mismatched chart lengths for " + symbol);
        }

        List<String> rows = new ArrayList<>();
        for (int i = 0; i < rawDates.size(); i++) {
            LocalDate date = LocalDate.parse(String.valueOf(rawDates.get(i)), DATE_FORMATTER);
            if (date.isBefore(CUTOFF_DATE)) {
                continue;
            }

            double price = ((Number) rawPrices.get(i)).doubleValue();
            rows.add(symbol + "," + date + "," + price);
        }

        return rows;
    }

    private static void waitForChartRefresh() {
        try {
            Thread.sleep(CHART_REFRESH_WAIT_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for TEFAS chart refresh", exception);
        }
    }
}
