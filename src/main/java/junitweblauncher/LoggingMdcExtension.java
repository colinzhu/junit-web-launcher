package junitweblauncher;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.MDC;

public class LoggingMdcExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        String className = context.getRequiredTestClass().getSimpleName();
        String methodName = context.getRequiredTestMethod().getName();

        MDC.put("testClass", className);
        MDC.put("testMethod", methodName);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        MDC.remove("testClass");
        MDC.remove("testMethod");
    }
}
