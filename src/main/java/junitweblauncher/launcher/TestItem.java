package junitweblauncher.launcher;

public record TestItem(String className, String methodName, String classDisplayName, String methodDisplayName, String fullyQualifiedMethodName) {

}
