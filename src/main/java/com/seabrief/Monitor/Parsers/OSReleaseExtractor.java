package com.seabrief.Monitor.Parsers;

import java.io.BufferedReader;
import java.io.FileReader;

public class OSReleaseExtractor {
    public static OSRelease extract(String filePath) throws Exception {
        String line;
        OSRelease os_release = new OSRelease();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");

                String key = parts[0];
                String value = parts[1].replace("\"", "");

                if (key.equals("NAME")) {
                    os_release.name = value;
                }

                if (key.equals("VERSION")) {
                    os_release.version = value;
                }
            }
        }

        if (os_release.name == null || os_release.version == null) {
            throw new Exception("Could not get name or version from os-release file");
        }

        return os_release;
    }
}