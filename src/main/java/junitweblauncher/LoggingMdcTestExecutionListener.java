package junitweblauncher;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.MDC;

public class LoggingMdcTestExecutionListener implements TestExecutionListener {

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        if (testIdentifier.isTest()) {
            String className = testIdentifier.getSource().get().getClass().getSimpleName();
            String methodName = testIdentifier.getDisplayName();
            // Add class and method to the MDC
            MDC.put("testClass", className);
            MDC.put("testMethod", methodName);
        }
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        // Clear the MDC after each test
        MDC.clear();
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        // Optionally handle something when the test plan starts
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        // Optionally handle something when the test plan finishes
    }
}
