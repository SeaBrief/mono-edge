package com.seabrief.Services.Tools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String GREEN_PREFIX = "\u001B[32m";
    private static final String RED_PREFIX = "\u001B[31m";
    private static final String LIGHTBLUE_PREFIX = "\u001B[94m";
    private static final String RESET_COLOR = "\u001B[0m";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String message) {
        _log(message, false);
    }

    public static void logWithTime(String message) {
        _log(message, true);
    }

    public static void error(String message) {
        _error(message, false);
    }

    public static void errorWithTime(String message) {
        _error(message, true);
    }

    private static void _log(String message, boolean time) {
        String label = "LOG";
        String timestamp = formatter.format(LocalDateTime.now());
        String logMessage = "";

        String pattern = time ? "%s [%s] %s" : "[%s] %s";

        if (System.console() == null) {
            logMessage = time ? String.format(pattern, timestamp, label, message)
                    : String.format(pattern, label, message);
        } else {
            logMessage = time ? String.format(pattern, LIGHTBLUE_PREFIX + timestamp + RESET_COLOR,
                    GREEN_PREFIX + label + RESET_COLOR, message)
                    : String.format(pattern,
                            GREEN_PREFIX + label + RESET_COLOR, message);
        }

        System.out.println(logMessage);
    }

    private static void _error(String message, boolean time) {
        String label = "ERR";
        String timestamp = formatter.format(LocalDateTime.now());
        String logMessage = "";

        String pattern = time ? "%s [%s] %s" : "[%s] %s";

        if (System.console() == null) {
            logMessage = time ? String.format(pattern, timestamp, label, message)
                    : String.format(pattern, label, message);
        } else {
            logMessage = time
                    ? String.format(pattern, LIGHTBLUE_PREFIX + timestamp + RESET_COLOR,
                            RED_PREFIX + label + RESET_COLOR, message)
                    : String.format(pattern, RED_PREFIX + label + RESET_COLOR, message);
        }

        System.err.println(logMessage);
    }
}
