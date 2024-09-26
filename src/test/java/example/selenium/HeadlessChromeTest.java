package example.selenium;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HeadlessChromeTest {

    private WebDriver driver;

    @BeforeEach
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "/home/colin/dev/chromedriver-linux64/chromedriver");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run in headless mode
        options.addArguments("--no-sandbox"); // Bypass OS security model
        options.addArguments("--disable-dev-shm-usage"); // Overcome limited resource problems
        options.addArguments("--window-size=800,680");

        driver = new ChromeDriver(options);
    }

    @Test
    @DisplayName("Purpose: able to open bing.com from a browser")
    @SneakyThrows
    public void testPageTitle() {
        Allure.step("Open URl and verify title" , () -> openUrl("https://cn.bing.com/", "Bing"));
    }

    @Step("Open URL: {0} and verify title: {1}")
    private void openUrl(String url, String expectedTitle) throws IOException {
        driver.get(url);
        // Capture screenshot as bytes
        byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(screenshotBytes)) {
            Allure.addAttachment("Screenshot", "image/png", inputStream, "png");
        }
        String title = driver.getTitle();
        assertEquals(expectedTitle, title);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
