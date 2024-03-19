package com.seabrief.Logger.Parser.Extractors.Blob;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.seabrief.Logger.Models.CDPDataStore;
import com.seabrief.Logger.Models.Metadata;
import com.seabrief.Logger.Parser.Extractors.SQL.ReadOnlyDatabase;
import com.seabrief.Logger.Parser.Extractors.SQL.SQL;

public class Unpacker {
    private ReadOnlyDatabase database;
    private CDPDataStore type;
    private HashMap<Integer, Metadata> metadata;
    private HashMap<String, Integer> lookup;
    private String keyframesTable = "Keyframes0";

    public Unpacker(String filePath, CDPDataStore type) throws SQLException {
        this.type = type;
        this.database = new ReadOnlyDatabase(filePath);

        loadMetadata();
    }

    private void loadMetadata() throws SQLException {
        metadata = new HashMap<>();
        lookup = new HashMap<>();

        String sql = new SQL()
                .pattern("SELECT * FROM  @table")
                .field("table", getSignalTable())
                .build();

        database.query(sql, (rs) -> {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            String type = rs.getString("type");
            String path = rs.getString("path");

            Metadata current = new Metadata(id, name, type, path);
            metadata.put(current.getId(), current);
            lookup.put(name, id);
        });
    }

    public HashMap<Integer, Metadata> getSignals() {
        return metadata;
    }

    public HashMap<Long, Double> getLastKeyframeBefore(String signal, long from, long to)
            throws SQLException {
        HashMap<Long, Double> values = new HashMap<>();

        String sql = new SQL()
                .pattern("SELECT x_axis, @column FROM @table WHERE x_axis BETWEEN @from AND @to")
                .field("column", getKeyframesColumn(signal))
                .field("table", keyframesTable)
                .field("from", (double) from / 1000)
                .field("to", (double) to / 1000)
                .build();

        database.query(sql, (rs) -> {
            long timestamp = (long) (rs.getDouble("x_axis") * 1000);
            double value = rs.getDouble(signal + "Last");
            values.put(timestamp, value);
        });

        return values;
    }

    public HashMap<Long, Double> getRange(String signal, long from, long to) throws Exception {
        try {
            HashMap<Long, Double> values = new HashMap<>();

            String sql = new SQL()
                    .pattern("SELECT * FROM @table WHERE x_axis BETWEEN @from AND @to")
                    .field("table", getBlobTable())
                    .field("from", (double) from / 1000)
                    .field("to", (double) to / 1000)
                    .build();

            database.query(sql, (rs) -> {
                long timestamp = (long) (rs.getDouble("x_axis") * 1000);
                HashMap<Integer, Double> decodedValues = new HashMap<>();

                if (type == CDPDataStore.SPLIT) {
                    decodedValues = BlobParser.decodeSplitBlob(rs.getBytes("y_axis_data"));
                } else {
                    decodedValues = BlobParser.decodeCompactBlob(rs.getBytes("y_axis_data"), metadata);
                }

                if (decodedValues.get(lookup.get(signal)) != null) {
                    values.put(timestamp, decodedValues.get(lookup.get(signal)));
                }
            });

            if (values.isEmpty()) {
                return getLastKeyframeBefore(signal, from, to);
            }

            return values;
        } catch (Exception ex) {
            String message = "Failed to getRange for signal " + signal
                    + "\nin database " + database.getURL()
                    + "\nin table " + getBlobTable()
                    + "\nin range " + from + " => " + to;
            throw new Exception(message, ex);
        }
    }

    private long getFrom() throws SQLException {
        AtomicLong from = new AtomicLong(-1);

        String sql = new SQL()
                .pattern("SELECT x_axis FROM @table ORDER BY x_axis ASC LIMIT 1")
                .field("table", keyframesTable)
                .build();

        database.query(sql, (rs) -> {
            from.set((long) (rs.getDouble("x_axis") * 1000));
        });

        return from.get();
    }

    private long getTo() throws SQLException {
        AtomicLong to = new AtomicLong(-1);

        String sql = new SQL()
                .pattern("SELECT x_axis FROM @table ORDER BY x_axis DESC LIMIT 1")
                .field("table", keyframesTable)
                .build();

        database.query(sql, (rs) -> {
            to.set((long) (rs.getDouble("x_axis") * 1000));
        });

        return to.get();
    }

    public Long[] getBounds() throws SQLException {
        long from = getFrom();
        long to = getTo();

        if (from == -1 || to == -1) {
            throw new RuntimeException("Could not retrive bounds from: " + database.getURL());
        }

        return new Long[] { from, to };
    }

    private String getKeyframesColumn(String signal) {
        return "`" + signal + "Last" + "`";
    }

    private String getBlobTable() {
        return type == CDPDataStore.SPLIT ? "NodeValues" : "SignalValues";
    }

    private String getSignalTable() {
        return type == CDPDataStore.SPLIT ? "Node" : "SignalMap";
    }
}
