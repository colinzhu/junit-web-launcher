package junitweblauncher.launcher;

import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.Extension;
import io.qameta.allure.ReportGenerator;
import io.qameta.allure.allure1.Allure1Plugin;
import io.qameta.allure.allure2.Allure2Plugin;
import io.qameta.allure.category.CategoriesPlugin;
import io.qameta.allure.category.CategoriesTrendPlugin;
import io.qameta.allure.context.FreemarkerContext;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.context.MarkdownContext;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.*;
import io.qameta.allure.duration.DurationPlugin;
import io.qameta.allure.duration.DurationTrendPlugin;
import io.qameta.allure.environment.Allure1EnvironmentPlugin;
import io.qameta.allure.executor.ExecutorPlugin;
import io.qameta.allure.history.HistoryPlugin;
import io.qameta.allure.history.HistoryTrendPlugin;
import io.qameta.allure.idea.IdeaLinksPlugin;
import io.qameta.allure.influxdb.InfluxDbExportPlugin;
import io.qameta.allure.launch.LaunchPlugin;
import io.qameta.allure.mail.MailPlugin;
import io.qameta.allure.owner.OwnerPlugin;
import io.qameta.allure.prometheus.PrometheusExportPlugin;
import io.qameta.allure.retry.RetryPlugin;
import io.qameta.allure.retry.RetryTrendPlugin;
import io.qameta.allure.severity.SeverityPlugin;
import io.qameta.allure.status.StatusChartPlugin;
import io.qameta.allure.suites.SuitesPlugin;
import io.qameta.allure.summary.SummaryPlugin;
import io.qameta.allure.tags.TagsPlugin;
import io.qameta.allure.timeline.TimelinePlugin;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import io.qameta.allure.DummyReportGenerator;

import static junitweblauncher.launcher.TestUtils.testIdentifierToTestMethod;

@Slf4j
class ReportListener implements TestExecutionListener {

    public static final String FOLDER_ALLURE_RESULTS = "allure-results";
    public static final String FOLDER_ALLURE_REPORTS = "allure-reports";
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
        if (!FileUtils.deleteQuietly(new File(FOLDER_ALLURE_RESULTS))) {
            log.warn("Error deleting directory");
        }
        log.info("testPlan execution started. runId:{}", runId);
        this.testPlan = testPlan;
        summaryGeneratingListener.testPlanExecutionStarted(testPlan);
        reportItemMap = new ConcurrentHashMap<>();
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        log.info("testPlan execution finished");
        summaryGeneratingListener.testPlanExecutionFinished(testPlan);
        this.summary = summaryGeneratingListener.getSummary();
        this.runReportItems = reportItemMap.values().stream().toList();
        printToLog();
        try {
            String reportFolder = FOLDER_ALLURE_REPORTS + "/" + runId;
            //DummyReportGenerator.main(FOLDER_ALLURE_RESULTS, reportFolder);
            generateSingleFileReport(Path.of(reportFolder), Path.of(FOLDER_ALLURE_RESULTS));
            log.info("Allure report generated to {}", reportFolder);
        } catch (IOException e) {
            log.info("Error generating allure report", e);
        }
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
            log.info("[{}][{}] test execution started", id.getDisplayName(), id.getUniqueId());
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

    private static void generateSingleFileReport(Path outputDir, Path... resultDirs) throws IOException {
        final List<Plugin> plugins = DummyReportGenerator.loadPlugins();
        final Configuration configuration = new ConfigurationBuilder()
                .withExtensions(EXTENSIONS)
                .withPlugins(plugins)
                .withReportName("Report")
                .build();
        final ReportGenerator generator = new ReportGenerator(configuration);
        generator.generateSingleFile(outputDir, Arrays.stream(resultDirs).toList());
    }

    private static final List<Extension> EXTENSIONS = Arrays.asList(
            new JacksonContext(),
            new MarkdownContext(),
            new FreemarkerContext(),
            new RandomUidContext(),
            new MarkdownDescriptionsPlugin(),
            new TagsPlugin(),
            new RetryPlugin(),
            new RetryTrendPlugin(),
            new SeverityPlugin(),
            new OwnerPlugin(),
            new IdeaLinksPlugin(),
            new HistoryPlugin(),
            new HistoryTrendPlugin(),
            new CategoriesPlugin(),
            new CategoriesTrendPlugin(),
            new DurationPlugin(),
            new DurationTrendPlugin(),
            new StatusChartPlugin(),
            new TimelinePlugin(),
            new SuitesPlugin(),
            new TestsResultsPlugin(),
            new AttachmentsPlugin(),
            new MailPlugin(),
            new InfluxDbExportPlugin(),
            new PrometheusExportPlugin(),
            new SummaryPlugin(),
            new ExecutorPlugin(),
            new LaunchPlugin(),
            new Allure1Plugin(),
            new Allure1EnvironmentPlugin(),
            new Allure2Plugin()
    );
}