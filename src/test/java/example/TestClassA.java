package example;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Slf4j
public class TestClassA {
    @Test
    @SneakyThrows
    public void testA_method1() {
        log.info("TestClassA.testA_method1");
        Assertions.assertTrue(false);
    }

    @ParameterizedTest
    @CsvSource({"P1", "P2"})
    public void testA_method2(String value) {
        log.info("TestClassA.testA_method2, {}", value);
        Assertions.assertTrue(false);
    }

}
