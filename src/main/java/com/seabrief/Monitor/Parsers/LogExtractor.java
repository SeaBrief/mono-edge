package com.seabrief.Monitor.Parsers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogExtractor {
    private static final String SERVICE_FILE = "services.txt";
    private static final String LOG_FILE = "log_output.txt";

    public static String getFile() throws IOException, InterruptedException {
        int service_status = exec(String.format("systemctl list-units --type=service --state=active > %s", SERVICE_FILE));

        if (service_status != 0) {
            throw new IOException("Failed to write services to file");
        }

        String service_name = getCDPService(SERVICE_FILE);

        int log_status = exec(String.format("journalctl -u %s > %s", service_name, LOG_FILE));

        if (log_status != 0) {
            throw new IOException("Failed to write services to file");
        }

        return LOG_FILE;
    }

    public static String getCDPService(String service_file) throws FileNotFoundException, IOException {
        String servicePattern = "\\s+(\\w+\\.\\w+)\\s+\\w+\\s+(\\w+)\\s+\\w+\\s+(.+)";

        String line;
        String service_name = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(service_file))) {
            reader.readLine(); // skip first
            while ((line = reader.readLine()) != null) {
                Pattern pattern = Pattern.compile(servicePattern);

                Matcher matcher = pattern.matcher(line);

                if (matcher.matches()) {
                    String unit = matcher.group(1);

                    if (unit.toLowerCase().startsWith("cdp")) {
                        service_name = unit;
                    }
                }
            }
        }

        return service_name;
    }

    private static int exec(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        return process.waitFor();
    }
}
