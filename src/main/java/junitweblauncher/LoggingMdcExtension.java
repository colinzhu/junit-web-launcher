package junitweblauncher;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.MDC;

@Slf4j
public class LoggingMdcExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        String className = context.getRequiredTestClass().getSimpleName();
        String methodName = context.getRequiredTestMethod().getName();
        String displayName = context.getDisplayName();

        MDC.put("testClass", className);
        MDC.put("testMethod", methodName);
        MDC.put("testDisplayName", displayName);
        log.info("Starting test execution");
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        log.info("Finished test execution");
        MDC.remove("testClass");
        MDC.remove("testMethod");
        MDC.remove("testDisplayName");
    }
}
