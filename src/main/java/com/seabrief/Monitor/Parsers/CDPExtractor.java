package com.seabrief.Monitor.Parsers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CDPExtractor {
    private final String VERSION_PATTERN = "\\d+\\.\\d+\\.\\d+";
    private final String ADDRESS_PATTERN = "\\d+\\.\\d+\\.\\d+\\.\\d+\\:\\d+";
    private final String CONNECTING_PATTERN = "Connecting\\s+App\\s+(\\d+):\\s+(\\S+)\\s+Ip:\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+)";
    private final String ROOT_APPLICATION = "LoggerApp";

    private String filePath;

    private HashMap<String, CDPApplication> applications;

    public CDPExtractor(String filePath) {
        this.applications = new HashMap<>();
        this.filePath = filePath;

        this.applications.put(ROOT_APPLICATION, new CDPApplication(ROOT_APPLICATION));
    }

    public ArrayList<CDPApplication> getApplications() {
        return new ArrayList<>(applications.values());
    }

    public void extract() throws FileNotFoundException, IOException {
        String line;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("--")) {
                    continue;
                }

                if (line.startsWith("*")) {
                    continue;
                }

                if (line.contains("Connecting")) {
                    onAppConnecting(line);
                }

                if (line.toLowerCase().contains("CDP Version".toLowerCase())) {
                    onVersion(line);
                }

                if (line.contains("ip-address")) {
                    onLoggerConnected(line);
                }
            }

            propagateVersion();
        }
    }

    private void propagateVersion() {
        String version = applications.get(ROOT_APPLICATION).version;
        
        for (String application : applications.keySet()) {
            applications.get(application).version = version;
        }
    }

    private void onVersion(String line) {
        String version = extractSingle(line, VERSION_PATTERN);
        applications.get(ROOT_APPLICATION).version = version;
    }

    private void onAppConnecting(String line) {
        String app = extractMultiple(line, CONNECTING_PATTERN, 2);
        String address = extractSingle(line, ADDRESS_PATTERN);

        if (app == null) {
            return;
        }

        if (applications.containsKey(app)) {
            applications.get(app).address = address;
        } else {
            applications.put(app, new CDPApplication(app, address));
        }
    }

    private void onLoggerConnected(String line) {
        String address = extractSingle(line, ADDRESS_PATTERN);
        applications.get(ROOT_APPLICATION).address = address;
    }

    private String extractSingle(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(input);

        if (!matcher.find()) {
            return null;
        }

        return matcher.group();
    }

    private String extractMultiple(String input, String regex, int group) {
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(input);

        if (!matcher.find()) {
            return null;
        }

        return matcher.group(group);
    }
}
