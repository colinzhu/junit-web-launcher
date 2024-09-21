package example.p2;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
@DisplayName("""
        Given there is one message with message type ABC
        When it's send to the service
        Then the message should be saved in the service
        """)
public class TestClassB {
    @Test
    @DisplayName("****TestB_method1****")
    public void testB_method1() {
        log.info("this is TestClassB.testB_method1");
        Assertions.assertTrue(true);
    }

    @Test
    public void testB_method2() {
        log.info("this is TestClassB.testB_method2");
        Assertions.assertTrue(true);
    }

}
