package junitweblauncher.listener;

import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.DummyReportGenerator;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class AllureReportListener implements TestExecutionListener {

    public static final String FOLDER_ALLURE_RESULTS = "allure-results";
    public static final String FOLDER_ALLURE_REPORTS = "allure-reports";

    private final Supplier<String> reportNameSupplier;
    private final Consumer<Path> postReportProcessor;

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        if (!FileUtils.deleteQuietly(new File(FOLDER_ALLURE_RESULTS))) {
            log.warn("Error deleting directory");
        }
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        try {
            String reportName = reportNameSupplier.get();
            String reportFolder = FOLDER_ALLURE_REPORTS + "/" + reportName;
            //DummyReportGenerator.main(FOLDER_ALLURE_RESULTS, reportFolder);
            generateSingleFileReport(Path.of(reportFolder), Path.of(FOLDER_ALLURE_RESULTS));
            log.info("Allure report generated to {}", reportFolder);
            if (postReportProcessor != null) {
                try {
                    postReportProcessor.accept(Path.of(reportFolder, "index.html"));
                } catch (Exception e) {
                    log.error("Error invoking postReportProcessor", e);
                }
            }
        } catch (IOException e) {
            log.info("Error generating allure report", e);
        }
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