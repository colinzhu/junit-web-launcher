package junitweblauncher.launcher;

import java.util.List;

public interface LauncherAdapter {
    List<TestMethod> listTestMethods(String packageName);
    void runTestMethods(List<String> testMethods);
}
