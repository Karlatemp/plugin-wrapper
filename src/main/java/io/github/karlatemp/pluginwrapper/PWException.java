package io.github.karlatemp.pluginwrapper;

public class PWException extends RuntimeException {
    public final int code;

    public PWException(int code, String message) {
        super(message);
        this.code = code;
    }

    public PWException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
