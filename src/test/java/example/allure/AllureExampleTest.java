package example.allure;

import io.qameta.allure.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

//@ExtendWith(LoggingMdcExtension.class)
@Slf4j
public class AllureExampleTest {

    @Link(name = "TestPlan-123", url = "http://www.bing.com")
    @Epic("Epic1")
    @Feature("Feature1")
    @Story("Story1")
    @ParameterizedTest(name = "{displayName}-#{index}-{arguments}")
    @CsvSource({"P1", "P2"})
    @DisplayName("Epic Feature Store")
    public void epicFeatureStore(@Param("value") String value) {
        Allure.step("Write a debug log", () -> log.info("TestClassA.testA_method3, {}", value));
        Assertions.assertTrue(true,"Value of the result should be true");
        Allure.step("Example step2");
        Allure.step("step3", this::exampleStep);
        Allure.addAttachment("Request message", "{abcdefg}");
    }

    private void exampleStep() {
        log.info("test method");
    }
}
