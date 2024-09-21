package junitweblauncher.launcher;

import lombok.extern.slf4j.Slf4j;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

@Slf4j
public class LauncherAdapterImpl implements LauncherAdapter {
    @Override
    public List<TestItem> listTestItems(String packageName, String listType) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage(packageName))
                .build();

        Launcher launcher = LauncherFactory.create();
        TestPlan testPlan = launcher.discover(request);

        List<TestItem> testItems = new ArrayList<>();
        if ("classes".equals(listType)) {
            testPlan.getRoots().forEach(root -> listTestClasses(testItems, testPlan, root));
        } else {
            testPlan.getRoots().forEach(root -> listTestMethods(testItems, testPlan, root));
        }

        log.info("Found test items count: {}", testItems.size());
        return testItems;
    }

    public RunReport runTestMethods(List<String> fullyQualifiedNames) {
        List<MethodSelector> methodSelectors = fullyQualifiedNames.stream().filter(i -> i.contains("#")).map(DiscoverySelectors::selectMethod).toList();
        List<ClassSelector> classSelectors = fullyQualifiedNames.stream().filter(i -> !i.contains("#")).map(DiscoverySelectors::selectClass).toList();
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(methodSelectors)
                .selectors(classSelectors)
                .build();

        ReportListener reportListener = new ReportListener();
        Launcher launcher = LauncherFactory.create();
        launcher.execute(request, reportListener);

        String summary = prepareSummary(reportListener.getSummary());
        return new RunReport(reportListener.getRunId(), summary, reportListener.getRunTestItems());
    }

    private static String prepareSummary(TestExecutionSummary summary) {
        summary.getFailures().forEach(failure -> {
            TestItem testItem = testIdentifierToTestMethod(null, failure.getTestIdentifier());
            log.error("[{}][{}][{}] Failed:", testItem.className(), testItem.methodName(), testItem.methodDisplayName(), failure.getException());
        });
        StringWriter stringWriter = new StringWriter();
        summary.printTo(new PrintWriter(stringWriter));

        log.info("\n{}", stringWriter);
        return stringWriter.toString();
    }

    private static void listTestMethods(List<TestItem> testItems, TestPlan testPlan, TestIdentifier parent) {
        testPlan.getChildren(parent).forEach(child -> {
            if (isPureContainer(child)) {
                listTestMethods(testItems, testPlan, child);
            } else {
                log.info("Found testMethods: {}_{}", child.getType(), child.getUniqueId());
                testItems.add(testIdentifierToTestMethod(parent.getDisplayName(), child)); // when listing methods, put parent display name (class display name)
            }
        });
    }

    private static void listTestClasses(List<TestItem> testItems, TestPlan testPlan, TestIdentifier parent) {
        testPlan.getChildren(parent).forEach(child -> {
            if (!isClassContainer(child)) {
                listTestClasses(testItems, testPlan, child);
            } else {
                log.info("Found testClasses: {}_{}", child.getType(), child.getUniqueId());
                testItems.add(testIdentifierToTestClass(child));
            }
        });
    }

    private static boolean isPureContainer(TestIdentifier child) {
        return child.getType() == TestDescriptor.Type.CONTAINER
                && child.getUniqueIdObject().getSegments().stream().noneMatch(s -> s.getType().equals("test-template"));
    }

    private static boolean isClassContainer(TestIdentifier child) {
        return child.getType() == TestDescriptor.Type.CONTAINER
                && child.getUniqueIdObject().getSegments().stream().noneMatch(s -> s.getType().equals("test-template"))
                && child.getUniqueIdObject().getSegments().stream().anyMatch(s -> s.getType().equals("class"));
    }

    private static TestItem testIdentifierToTestMethod(String parentDisplayName, TestIdentifier testId) {
        Map<String, String> segments = new HashMap<>();
        testId.getUniqueIdObject().getSegments().stream()
                .filter(segment -> List.of("class", "method", "test-template").contains(segment.getType()))
                .forEach(segment -> segments.put(segment.getType(), segment.getValue()));
        String className = segments.get("class");
        String methodName = segments.get("method") == null ? segments.get("test-template") : segments.get("method");
        String itemDisplayName = testId.getDisplayName();
        return new TestItem(className, methodName, parentDisplayName, itemDisplayName, Stream.of(className, methodName).filter(Objects::nonNull).collect(Collectors.joining("#")));
    }

    private static TestItem testIdentifierToTestClass(TestIdentifier testId) {
        String className = TestUtils.getTestClass(testId);
        return new TestItem(className, null, testId.getDisplayName(), null, className);
    }


}
