package example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class TestClassB {
    @Test
    public void testB_method1() {
        log.info("TestClassB.testB_method1");
        Assertions.assertTrue(true);
    }

    @Test
    public void testB_method2() {
        log.info("TestClassB.testB_method2");
        Assertions.assertTrue(true);
    }

}
