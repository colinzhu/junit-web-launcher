package junitweblauncher.launcher;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.util.HashMap;
import java.util.Map;

@Slf4j
class ReportListener implements TestExecutionListener {

    private final SummaryGeneratingListener summaryGeneratingListener;

    @Getter
    private Map<String, LauncherAdapter.RunReportItem> runTestItems;
    @Getter
    private String runId;
    @Getter
    private TestExecutionSummary summary;

    public ReportListener() {
        this.summaryGeneratingListener = new SummaryGeneratingListener();
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        System.out.println("\r\n");
        log.info("testPlan execution started");
        summaryGeneratingListener.testPlanExecutionStarted(testPlan);
        runTestItems = new HashMap<>();
        runId = System.getProperty("runId");
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        summaryGeneratingListener.testPlanExecutionFinished(testPlan);
        this.summary = summaryGeneratingListener.getSummary();
        log.info("testPlan execution finished");
    }

    @Override
    public void dynamicTestRegistered(TestIdentifier testIdentifier) {
        summaryGeneratingListener.dynamicTestRegistered(testIdentifier);
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        summaryGeneratingListener.executionSkipped(testIdentifier, reason);
    }

    @Override
    public void executionStarted(TestIdentifier id) {
        summaryGeneratingListener.executionStarted(id);
        if (id.isTest()) {
            LauncherAdapter.RunReportItem runReportItem = new LauncherAdapter.RunReportItem(TestUtils.getTestClass(id), TestUtils.getInvocationOrMethodName(id),
                    System.currentTimeMillis(), 0, "STARTED", null);
            runTestItems.put(id.getUniqueId(), runReportItem);
        }
    }

    @Override
    public void executionFinished(TestIdentifier id, TestExecutionResult testExecutionResult) {
        summaryGeneratingListener.executionFinished(id, testExecutionResult);
        if (id.isTest()) {
            LauncherAdapter.RunReportItem runReportItem = runTestItems.get(id.getUniqueId());
            runReportItem.setEndTime(System.currentTimeMillis());
            runReportItem.setStatus(testExecutionResult.getStatus().name());
            runReportItem.setException(testExecutionResult.getThrowable().orElse(null));
        }
    }

    @Override
    public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
        summaryGeneratingListener.reportingEntryPublished(testIdentifier, entry);
    }

}