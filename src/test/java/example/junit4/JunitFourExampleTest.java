package example.junit4;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Slf4j
public class JunitFourExampleTest {
    @Test
    public void testJunit4Demo() {
        log.info("Junit 4 test demo");
        assertEquals("Junit 4 test demo", 1, 1);
    }
    @Test
    @Ignore("Ignore this test for demo")
    public void testJunit4Demo2() {
        log.info("Junit 4 test demo2");
        assertEquals("Junit 4 test demo2", 1, 1);
    }
}
