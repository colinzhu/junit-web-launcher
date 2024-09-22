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
                .filter(segment -> "class".equals(segment.getType()) || "runner".equals(segment.getType()))
                .findFirst().map(UniqueId.Segment::getValue).orElse(null);
    }

    static LauncherAdapter.TestItem testIdentifierToTestMethod(String parentDisplayName, TestIdentifier testId) {
        Map<String, String> segments = new HashMap<>();
        testId.getUniqueIdObject().getSegments().stream()
                .filter(segment -> List.of("class", "method", "test-template", "runner").contains(segment.getType()))
                .forEach(segment -> segments.put(segment.getType(), segment.getValue()));
        String className = segments.get("class") == null ? segments.get("runner") : segments.get("class");
        String methodName = segments.get("method") == null ? segments.get("test-template") : segments.get("method");
        methodName = methodName == null ? testId.getDisplayName() : methodName; // for junit4 test
        String itemDisplayName = testId.getDisplayName();
        return new LauncherAdapter.TestItem(className, methodName, parentDisplayName, itemDisplayName, Stream.of(className, methodName).filter(Objects::nonNull).collect(Collectors.joining("#")));
    }
}
