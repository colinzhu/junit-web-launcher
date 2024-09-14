package junitweblauncher.launcher;

import java.util.List;

public interface LauncherAdapter {
    List<TestMethod> listCases();
    void runCases(List<TestMethod> testMethods);
}
