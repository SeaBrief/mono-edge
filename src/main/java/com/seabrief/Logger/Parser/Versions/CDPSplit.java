package com.seabrief.Logger.Parser.Versions;

import java.io.File;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.seabrief.Logger.Models.CDPDataStore;
import com.seabrief.Logger.Parser.Extractors.Blob.Unpacker;
import com.seabrief.Logger.Parser.Extractors.SQL.ReadOnlyDatabase;

public class CDPSplit implements ICDPParser {
    private ReadOnlyDatabase database;

    public HashMap<String, Integer> dictionary;

    private HashMap<Integer, Unpacker> partitions;

    public CDPSplit(String filePath) throws SQLException {
        this.database = new ReadOnlyDatabase(filePath);

        loadDictionary();
        loadPartitions(filePath);
    }

    private void loadPartitions(String root) throws SQLException {
        HashSet<Integer> unique = new HashSet<>(dictionary.values());
        ArrayList<Integer> indexes = new ArrayList<>(unique);

        partitions = new HashMap<>();

        for (Integer integer : indexes) {
            String filePath = getPartitionFile(root, integer);
            partitions.put(integer, new Unpacker(filePath, CDPDataStore.SPLIT));
        }
    }

    private String getPartitionFile(String root, int partition) {
        String[] filename = new File(root).getName().split("\\.");
        String dir = new File(root).getParent();
        return Paths.get(dir, filename[0] + partition + "." + filename[1]).toString();
    }

    private void loadDictionary() throws SQLException {
        dictionary = new HashMap<>();

        String sql = "SELECT name, connection FROM ConnectionNodeMap";

        database.query(sql, (rs) -> {
            String name = rs.getString("name");
            int partition = rs.getInt("connection");
            dictionary.put(name, partition);
        });
    }

    @Override
    public Long[] getBounds() throws SQLException {
        ArrayList<Long> bounds = new ArrayList<>();

        for (Unpacker unpacker : partitions.values()) {
            Long[] partitionBounds = unpacker.getBounds();
            bounds.add(partitionBounds[0]);
            bounds.add(partitionBounds[1]);
        }

        return new Long[] { Collections.min(bounds), Collections.max(bounds) };
    }

    @Override
    public HashMap<Long, Double> getRange(String signal, long from, long to) throws Exception {
        if (!dictionary.keySet().contains(signal)) {
            throw new InvalidParameterException("Signal " + signal + " was not found");
        }

        return partitions.get(dictionary.get(signal)).getRange(signal, from, to);
    }
}
