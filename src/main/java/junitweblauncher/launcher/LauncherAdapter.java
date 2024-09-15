package junitweblauncher.launcher;

import java.util.List;

public interface LauncherAdapter {
    List<TestItem> listTestItems(String packageName, String listType);
    void runTestMethods(List<String> testMethods);
}
