package com.seabrief.Logger.Parser.Extractors.XML;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.seabrief.Logger.Models.SignalMetadata;
import com.seabrief.Services.Tools.Logger;

public class XMLExtractor {
    private String rootPath;
    private Set<String> databases;
    private final String COMPONENTS_KEY = "Components";

    public XMLExtractor(String rootPath) {
        this.rootPath = rootPath;
        databases = new HashSet<>();
    }

    public HashMap<String, SignalMetadata> extract() throws IOException {
        String componentsDirectory = getComponentsDirectory(rootPath);

        if (componentsDirectory == null) {
            throw new FileNotFoundException("Cound not find Components folder from " + rootPath);
        }

        File[] fileList = new File(componentsDirectory).listFiles();

        Logger.log("Extracting System Information from: " + rootPath);
        Set<String> components = getComponents(fileList);
        return parseComponents(components);
    }

    public ArrayList<String> getDatabases() {
        return new ArrayList<String>(databases);
    }

    private String getComponentsDirectory(String rootPath) throws FileNotFoundException {
        File rootDirectory = new File(rootPath);

        if (!rootDirectory.exists()) {
            throw new FileNotFoundException("LoggerApp directory: " + rootPath + " could not be opened");
        }

        File[] fileList = rootDirectory.listFiles();

        if (fileList == null) {
            throw new FileNotFoundException("LoggerApp directory: " + rootPath + " opened, but could not access files");
        }

        if (rootPath.contains(COMPONENTS_KEY)) {
            int index = rootPath.indexOf(COMPONENTS_KEY);
            return rootPath.substring(0, index + COMPONENTS_KEY.length());
        }

        return searchForComponentsDirectory(rootPath);
    }

    private String searchForComponentsDirectory(String rootPath) throws FileNotFoundException {
        String directory = null;

        File[] fileList = new File(rootPath).listFiles();

        for (File file : fileList) {
            if (!file.isDirectory()) {
                continue;
            }

            if (file.getAbsolutePath().contains(COMPONENTS_KEY)) {
                directory = file.getAbsolutePath();
            } else {
                directory = searchForComponentsDirectory(file.getAbsolutePath());
            }
        }

        return directory;
    }

    private Set<String> getComponents(File[] fileList) throws IOException {
        Set<String> components = new HashSet<>();

        for (File file : fileList) {
            if (!file.isDirectory() && file.getName().endsWith(".xml")) {
                XMLFileParser fileParser = new XMLFileParser(file.getAbsolutePath()).parse();
                components.addAll(fileParser.getSubComponents());
            }
        }

        return components;
    }

    private HashMap<String, SignalMetadata> parseComponents(Set<String> components) throws IOException {
        HashMap<String, SignalMetadata> signals = new HashMap<>();

        for (String component : components) {
            File subComponent = new File(component);
            if (!subComponent.isDirectory() && subComponent.getName().endsWith(".xml")) {
                insertSignals(subComponent.getAbsolutePath(), signals);
            }
        }

        return signals;
    }

    private void insertSignals(String subComponent, HashMap<String, SignalMetadata> signals) throws IOException {
        XMLMetadataParser metadataParser = new XMLMetadataParser(subComponent).parse();
        String databasePath = metadataParser.getDatabaseName();
        HashMap<String, SignalMetadata> currentSignals = metadataParser.getSignals();

        if (databasePath != null && !currentSignals.isEmpty()) {
            File databaseFile = new File(databasePath);

            if (databaseFile == null || !databaseFile.exists()) {
                Logger.error("Dropping " + databasePath + " File Not Found!");
                return;
            }

            databases.add(databasePath);
            for (SignalMetadata signal : currentSignals.values()) {
                String route = getCompleteRoute(signal);
                String databaseFileName = new File(databasePath).getName().replace(".db", "");

                String key = databaseFileName + "." + signal.getKey();

                if (signals.get(key) == null) {
                    signal.setName(key);
                    signal.setRoute(route);
                    signal.setDatabasePath(databasePath);
                    signals.put(key, signal);
                } else {
                    SignalMetadata active = signals.get(key);
                    Logger.log("Found duplicate " + key);
                    Logger.log("\tnew: " + route);
                    Logger.log("\told: " + active.getRoute());
                }
            }
        }
    }

    private String getCompleteRoute(SignalMetadata signal) {
        String cleanedRoute = signal.getRoute().replace("...", "");
        return cleanedRoute;
    }
}
