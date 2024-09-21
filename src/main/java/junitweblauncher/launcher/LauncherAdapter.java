package junitweblauncher.launcher;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

public interface LauncherAdapter {
    List<TestItem> listTestItems(String packageName, String listType);

    RunReport runTestMethods(List<String> testMethods);

    record RunReport(String runId, String summary, List<RunReportItem> runReportItems) {

    }

    @Data
    @AllArgsConstructor
    class RunReportItem {
        private String className;
        private String methodName;
        private long startTime;
        private long endTime;
        private String status;
        private Throwable exception;
    }

    record TestItem(String className, String methodName, String classDisplayName, String methodDisplayName,
                    String fullyQualifiedMethodName) {

    }
}
