package example;

import junitweblauncher.LoggingMdcExtension;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

//@ExtendWith(LoggingMdcExtension.class)
@Slf4j
public class TestClassA {
    @Test
    @SneakyThrows
    public void testA_method1() {
        log.info("TestClassA.testA_method1");
        Assertions.assertTrue(false);
    }

    @ParameterizedTest(name = "{displayName} - #{index} - {arguments}")
    @CsvSource({"P1", "P2"})
    @DisplayName("TestA_method2")
    public void testA_method2(String value) {
        log.info("TestClassA.testA_method2, {}", value);
        Assertions.assertTrue(false);
    }

}
