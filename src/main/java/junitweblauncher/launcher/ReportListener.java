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
import java.util.stream.Collectors;

import static junitweblauncher.launcher.TestUtils.testIdentifierToTestMethod;

@Slf4j
class ReportListener implements TestExecutionListener {

    private final SummaryGeneratingListener summaryGeneratingListener;

    @Getter
    private String runId;
    private Map<String, LauncherAdapter.RunReportItem> reportItemMap;
    private TestExecutionSummary summary;
    private List<LauncherAdapter.RunReportItem> runReportItems;
    private TestPlan testPlan;

    public ReportListener() {
        this.summaryGeneratingListener = new SummaryGeneratingListener();
    }

    private void printToLog() {
        System.out.println();
        // print failed cases' exception stack trace
        summary.getFailures().forEach(failure -> {
            LauncherAdapter.TestItem testItem = testIdentifierToTestMethod(null, failure.getTestIdentifier());
            log.error("[{}][{}][{}] Failed:", testItem.className(), testItem.methodName(), testItem.methodDisplayName(), failure.getException());
        });

        // print all items status
        String allItemsStatusStr = runReportItems.stream().map(
                item -> "[%s][%s][%s][%s][%s][%s][%s]".formatted(item.getTestItem().className(), item.getTestItem().classDisplayName(), item.getTestItem().methodName(), item.getTestItem().methodDisplayName(), item.getStatus(), (item.getEndTime() - item.getStartTime()) + "ms", item.getReason())
                        .replace("\n", ".")
        ).collect(Collectors.joining("\n"));

        System.out.println();
        log.info("REPORT:\n{}\n{}", allItemsStatusStr, getSummaryString());
    }

    private String getSummaryString() {
        return String.format("%nTest run finished after %d ms "
                        + "[%5d tests found ]"
                        + "[%5d tests skipped ]"
                        + "[%5d tests started ]"
                        + "[%5d tests aborted ]"
                        + "[%5d tests successful ]"
                        + "[%5d tests failed ]",

                summary.getTimeFinished() - summary.getTimeStarted(),

                summary.getTestsFoundCount(),
                summary.getTestsSkippedCount(),
                summary.getTestsStartedCount(),
                summary.getTestsAbortedCount(),
                summary.getTestsSucceededCount(),
                summary.getTestsFailedCount());
    }

    LauncherAdapter.RunReport getRunReport() {
        return new LauncherAdapter.RunReport(runId, getSummaryString(), runReportItems);
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        System.out.println();
        runId = System.getProperty("runId");
        log.info("testPlan execution started. runId:{}", runId);
        this.testPlan = testPlan;
        summaryGeneratingListener.testPlanExecutionStarted(testPlan);
        reportItemMap = new HashMap<>();
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        log.info("testPlan execution finished");
        summaryGeneratingListener.testPlanExecutionFinished(testPlan);
        this.summary = summaryGeneratingListener.getSummary();
        this.runReportItems = reportItemMap.values().stream().toList();
        printToLog();
    }

    @Override
    public void dynamicTestRegistered(TestIdentifier testIdentifier) {
        summaryGeneratingListener.dynamicTestRegistered(testIdentifier);
    }

    @Override
    public void executionSkipped(TestIdentifier id, String reason) {
        if (id.isTest()) {
            long current = System.currentTimeMillis();
            String parentDisplayName = testPlan.getParent(id).map(TestIdentifier::getDisplayName).orElse(null);
            LauncherAdapter.RunReportItem runReportItem = new LauncherAdapter.RunReportItem(
                    TestUtils.testIdentifierToTestMethod(parentDisplayName, id),
                    current, current, "SKIPPED", reason, null, null);
            reportItemMap.put(id.getUniqueId(), runReportItem);
        }
        summaryGeneratingListener.executionSkipped(id, reason);
    }

    @Override
    public void executionStarted(TestIdentifier id) {
        summaryGeneratingListener.executionStarted(id);
        if (id.isTest()) {
            String parentDisplayName = testPlan.getParent(id).map(TestIdentifier::getDisplayName).orElse(null);
            LauncherAdapter.RunReportItem runReportItem = new LauncherAdapter.RunReportItem(
                    TestUtils.testIdentifierToTestMethod(parentDisplayName, id),
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