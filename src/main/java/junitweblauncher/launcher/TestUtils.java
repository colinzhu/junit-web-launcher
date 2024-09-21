package junitweblauncher.launcher;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.TestIdentifier;

class TestUtils {
    static String getTestClass(TestIdentifier testId) {
        return testId.getUniqueIdObject().getSegments().stream()
                .filter(segment -> "class".equals(segment.getType()))
                .findFirst().map(UniqueId.Segment::getValue).orElse(null);
    }

    static String getInvocationOrMethodName(TestIdentifier testId) {
        String invocation = testId.getUniqueIdObject().getSegments().stream()
                .filter(segment -> "test-template-invocation".equals(segment.getType()))
                .findFirst().map(UniqueId.Segment::getValue).orElse(null);

        if (invocation == null) { // if invocation name, then return method name
            return testId.getUniqueIdObject().getSegments().stream()
                    .filter(segment -> "method".equals(segment.getType()))
                    .findFirst().map(UniqueId.Segment::getValue).orElse(null);
        }
        return invocation;

    }
}
