package junitweblauncher;

import lombok.extern.slf4j.Slf4j;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

@Slf4j
public class App {
    public static void main(String[] args) {
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LoggingMdcTestExecutionListener mdcTestExecutionListener = new LoggingMdcTestExecutionListener();
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage("example"))
                .build();

        Launcher launcher = LauncherFactory.create();

        TestPlan testPlan = launcher.discover(request);
        testPlan.getRoots().forEach(root -> listTestCases(testPlan, root));
        launcher.execute(request, listener, mdcTestExecutionListener);

        TestExecutionSummary summary = listener.getSummary();
        System.out.println("Total tests: " + summary.getTestsFoundCount());
        summary.getFailures().forEach(failure -> {
            System.out.println("Failure: " + failure.getTestIdentifier().getDisplayName());
            System.out.println("  Reason: " + failure.getException().getMessage());
        });
    }

    private static void listTestCases(TestPlan testPlan, TestIdentifier parent) {
        testPlan.getChildren(parent).forEach(child -> {
            if (child.getType() == TestDescriptor.Type.CONTAINER) {
                listTestCases(testPlan, child);

            } else {
                log.info(child.getUniqueId());
            }
        });
    }

}