package junitweblauncher.launcher;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.TestIdentifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    static LauncherAdapter.TestItem testIdentifierToTestMethod(String parentDisplayName, TestIdentifier testId) {
        Map<String, String> segments = new HashMap<>();
        testId.getUniqueIdObject().getSegments().stream()
                .filter(segment -> List.of("class", "method", "test-template").contains(segment.getType()))
                .forEach(segment -> segments.put(segment.getType(), segment.getValue()));
        String className = segments.get("class");
        String methodName = segments.get("method") == null ? segments.get("test-template") : segments.get("method");
        String itemDisplayName = testId.getDisplayName();
        return new LauncherAdapter.TestItem(className, methodName, parentDisplayName, itemDisplayName, Stream.of(className, methodName).filter(Objects::nonNull).collect(Collectors.joining("#")));
    }
}
