package testers.test;

import java.util.ArrayList;

import logger.LogFileGenerator;
import serverDriver.SelendroidServerDriver;

class TesterTest {

    private static String defaultApkPath = "../employee-directory.apk";
    private static String defaultCapabilities = "io.selendroid.directory:0.0.1";

    static SelendroidServerDriver getDriver(String apkPath, String capabilities) {
        return new SelendroidServerDriver(apkPath, capabilities);
    }

    static SelendroidServerDriver getDriver() {
        return getDriver(defaultApkPath, defaultCapabilities);
    }

    static ArrayList<String> getTestInput() {
        ArrayList<String> testInput = new ArrayList<String>();

        String alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456890!@#$%^&*()_+,./<>?;':";
        char[] alphaArr = alpha.toCharArray();
        for (char c: alphaArr) {testInput.add(""+c);}
        return testInput;
    }

    static LogFileGenerator getLogFileGenerator() {
        return new LogFileGenerator("../JUnitTestLogs");
    }
}
