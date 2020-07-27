package io.github.karlatemp.pluginwrapper.test;

import io.github.karlatemp.pluginwrapper.PluginWrapper;

public class Tester {
    public static void main(String[] args) throws Throwable {
        PluginWrapper.invoke(
                "-o build -i G:/Java/KotlinRuntime-1.0.0.jar -t NATIVE --force".split(" ")
        );
    }
}
