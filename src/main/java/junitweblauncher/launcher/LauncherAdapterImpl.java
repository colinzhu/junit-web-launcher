package junitweblauncher.launcher;

import lombok.extern.slf4j.Slf4j;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

@Slf4j
public class LauncherAdapterImpl implements LauncherAdapter {
    @Override
    public List<TestMethod> listTestMethods(String packageName) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage(packageName))
                .build();

        Launcher launcher = LauncherFactory.create();
        TestPlan testPlan = launcher.discover(request);

        List<TestMethod> testMethods = new ArrayList<>();
        testPlan.getRoots().forEach(root -> listTestCases(testMethods, testPlan, root));

        log.info("Found test methods count: {}", testMethods.size());
        return testMethods;
    }

    public void runTestMethods(List<String> testMethods) {
        List<MethodSelector> methodSelectors = testMethods.stream().map(DiscoverySelectors::selectMethod).toList();
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(methodSelectors)
                .build();

        SummaryGeneratingListener summaryGeneratingListener = new SummaryGeneratingListener();
        Launcher launcher = LauncherFactory.create();
        launcher.execute(request, summaryGeneratingListener);

        TestExecutionSummary summary = summaryGeneratingListener.getSummary();
        System.out.println("Total tests: " + summary.getTestsFoundCount());
        summary.getFailures().forEach(failure -> {
            System.out.println("Failure: " + failure.getTestIdentifier().getDisplayName());
            System.out.println("  Reason: " + failure.getException().getMessage());
        });
    }

    private static void listTestCases(List<TestMethod> testMethods, TestPlan testPlan, TestIdentifier parent) {
        testPlan.getChildren(parent).forEach(child -> {
            if (isPureContainer(child)) {
                listTestCases(testMethods, testPlan, child);
            } else {
                log.info("Found case: {}_{}", child.getType(), child.getUniqueId());
                testMethods.add(testIdentifierToTestMethod(child));
            }
        });
    }

    private static boolean isPureContainer(TestIdentifier child) {
        return child.getType() == TestDescriptor.Type.CONTAINER
                && child.getUniqueIdObject().getSegments().stream().noneMatch(s -> s.getType().equals("test-template"));
    }


    private static TestMethod testIdentifierToTestMethod(TestIdentifier testId) {
        Map<String, String> segments = new HashMap<>();
        testId.getUniqueIdObject().getSegments().stream()
                .filter(segment -> List.of("class", "method", "test-template").contains(segment.getType()))
                .forEach(segment -> segments.put(segment.getType(), segment.getValue()));
        String className = segments.get("class");
        String methodName = segments.get("method") == null ? segments.get("test-template") : segments.get("method");
        return new TestMethod(className, methodName, className + "#" + methodName);
    }


}
