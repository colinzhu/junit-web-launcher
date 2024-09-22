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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junitweblauncher.launcher.TestUtils.testIdentifierToTestMethod;

@Slf4j
class ReportListener implements TestExecutionListener {

    private final SummaryGeneratingListener summaryGeneratingListener;

    private Map<String, LauncherAdapter.RunReportItem> reportItemMap;
    @Getter
    private String runId;
    private TestExecutionSummary summary;
    private List<LauncherAdapter.RunReportItem> runReportItems;

    public ReportListener() {
        this.summaryGeneratingListener = new SummaryGeneratingListener();
    }

    private void printToLog() {
        summary.getFailures().forEach(failure -> {
            LauncherAdapter.TestItem testItem = testIdentifierToTestMethod(null, failure.getTestIdentifier());
            log.error("[{}][{}][{}] Failed:", testItem.className(), testItem.methodName(), testItem.methodDisplayName(), failure.getException());
        });

        log.info("\n{}", getSummaryString());
    }

    private String getSummaryString() {
        StringWriter stringWriter = new StringWriter();
        summary.printTo(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    LauncherAdapter.RunReport getRunReport() {
        return new LauncherAdapter.RunReport(runId, getSummaryString(), runReportItems);
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        System.out.println("\r\n");
        runId = System.getProperty("runId");
        log.info("testPlan execution started. runId:{}", runId);
        summaryGeneratingListener.testPlanExecutionStarted(testPlan);
        reportItemMap = new HashMap<>();
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        summaryGeneratingListener.testPlanExecutionFinished(testPlan);
        this.summary = summaryGeneratingListener.getSummary();
        this.runReportItems = reportItemMap.values().stream().toList();
        printToLog();
        log.info("testPlan execution finished");
    }

    @Override
    public void dynamicTestRegistered(TestIdentifier testIdentifier) {
        summaryGeneratingListener.dynamicTestRegistered(testIdentifier);
    }

    @Override
    public void executionSkipped(TestIdentifier id, String reason) {
        if (id.isTest()) {
            long current = System.currentTimeMillis();
            LauncherAdapter.RunReportItem runReportItem = new LauncherAdapter.RunReportItem(TestUtils.getTestClass(id), TestUtils.getInvocationOrMethodName(id),
                    current, current, "SKIPPED", reason, null, null);
            reportItemMap.put(id.getUniqueId(), runReportItem);
        }
        summaryGeneratingListener.executionSkipped(id, reason);
    }

    @Override
    public void executionStarted(TestIdentifier id) {
        summaryGeneratingListener.executionStarted(id);
        if (id.isTest()) {
            LauncherAdapter.RunReportItem runReportItem = new LauncherAdapter.RunReportItem(TestUtils.getTestClass(id), TestUtils.getInvocationOrMethodName(id),
                    System.currentTimeMillis(), 0, "STARTED", null, null, null);
            reportItemMap.put(id.getUniqueId(), runReportItem);
        }
    }

    @Override
    public void executionFinished(TestIdentifier id, TestExecutionResult testExecutionResult) {
        summaryGeneratingListener.executionFinished(id, testExecutionResult);
        if (id.isTest()) {
            LauncherAdapter.RunReportItem runReportItem = reportItemMap.get(id.getUniqueId());
            runReportItem.setEndTime(System.currentTimeMillis());
            runReportItem.setStatus(testExecutionResult.getStatus().name());
            runReportItem.setReason(testExecutionResult.getThrowable().map(Throwable::getMessage).orElse(null));
            runReportItem.setException(testExecutionResult.getThrowable().orElse(null));
            runReportItem.setStackTrace(testExecutionResult.getThrowable().map(e -> {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                return sw.toString();
            }).orElse(null));
        }
    }

    @Override
    public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
        summaryGeneratingListener.reportingEntryPublished(testIdentifier, entry);
    }

}