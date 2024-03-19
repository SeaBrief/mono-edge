package com.seabrief.Services.Tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnvFile {
    public static void load(String[] required) throws IOException {
        String root = System.getProperty("user.dir");
        File file = new File(root, "envfile");

        if (!file.exists()) {
            throw new IOException(
                    String.format("Could not locate environment variables at '%s'", file.getAbsolutePath()));
        }

        List<String> requiredList = required == null ? null : Arrays.asList(required);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);

                if (parts.length != 2) {
                    continue;
                }

                String key = parts[0].trim();
                String value = parts[1].trim().replace("\"", "");

                if (requiredList != null && requiredList.contains(key)) {
                    List<String> newRequiredList = new ArrayList<>(requiredList);
                    newRequiredList.remove(key);
                    requiredList = newRequiredList;
                }

                System.setProperty(key, value);
            }
        }

        if (requiredList != null && !requiredList.isEmpty()) {
            for (String missing : requiredList) {
                Logger.error("Missing Environment Variable " + missing);
            }
            throw new IOException("Failed to Load Environment: Missing Variables");
        }

        Logger.log("Loaded Environment Variables from: " + file.getAbsolutePath());
    }

    public static String get(String key) {
        return System.getProperty(key);
    }
}
