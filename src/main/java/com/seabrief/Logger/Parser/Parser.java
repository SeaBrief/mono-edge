package com.seabrief.Logger.Parser;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.seabrief.Logger.Models.CDPDataStore;
import com.seabrief.Logger.Models.SignalMetadata;
import com.seabrief.Logger.Parser.Extractors.XML.XMLExtractor;
import com.seabrief.Logger.Parser.Versions.CDPBasic;
import com.seabrief.Logger.Parser.Versions.CDPCompact;
import com.seabrief.Logger.Parser.Versions.CDPSplit;
import com.seabrief.Logger.Parser.Versions.ICDPParser;
import com.seabrief.Logger.Parser.Versions.Discovery;

public class Parser implements ICDPParser {
    private static Parser instance = null;

    private HashMap<String, SignalMetadata> signals;
    private HashMap<String, ICDPParser> parsers;
    private ArrayList<String> databases;

    private Parser() {
        signals = new HashMap<>();
        parsers = new HashMap<>();
    }

    public static Parser getInstance() {
        if (instance == null) {
            synchronized (Parser.class) {
                if (instance == null) {
                    instance = new Parser();
                }
            }
        }

        return instance;
    }

    public void load(String root) throws IOException, SQLException {
        XMLExtractor extractor = new XMLExtractor(root);
        signals = extractor.extract();
        databases  = extractor.getDatabases();

        for (String databasePath : databases) {
            CDPDataStore type = Discovery.getDatabaseType(databasePath);

            if (type == CDPDataStore.BASIC) {
                parsers.put(databasePath, new CDPBasic(databasePath));
                continue;
            }

            if (type == CDPDataStore.COMPACT) {
                parsers.put(databasePath, new CDPCompact(databasePath));
                continue;
            }

            if (type == CDPDataStore.SPLIT) {
                parsers.put(databasePath, new CDPSplit(databasePath));
                continue;
            }

            throw new IllegalArgumentException("Database format not supported");
        }
    }

    public ArrayList<SignalMetadata> getSignals() {
        return new ArrayList<>(signals.values());
    }

    public ArrayList<String> getDatabases() {
        return databases;
    }

    @Override
    public Long[] getBounds() throws SQLException {
        ArrayList<Long> bounds = new ArrayList<>();

        for (ICDPParser parser : parsers.values()) {
            Long[] partitionBounds = parser.getBounds();
            bounds.add(partitionBounds[0]);
            bounds.add(partitionBounds[1]);
        }

        return new Long[] { Collections.min(bounds), Collections.max(bounds) };
    }

    @Override
    public HashMap<Long, Double> getRange(String route, long from, long to) throws Exception {
        if (!signals.keySet().contains(route)) {
            throw new InvalidParameterException("Signal key " + route + " was not found");
        }

        SignalMetadata match = signals.get(route);
        return parsers.get(match.getDatabasePath()).getRange(match.getKey(), from, to);
    }
}
