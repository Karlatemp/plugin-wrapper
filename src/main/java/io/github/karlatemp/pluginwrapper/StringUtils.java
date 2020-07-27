package io.github.karlatemp.pluginwrapper;

public class StringUtils {
    public static String getFileName(String name) {
        int index = name.lastIndexOf('.');
        if (index == -1) return name;
        return name.substring(0, index);
    }
}
