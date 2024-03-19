package com.seabrief.Logger.Parser.Versions;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.seabrief.Logger.Parser.Extractors.SQL.ReadOnlyDatabase;
import com.seabrief.Logger.Parser.Extractors.SQL.SQL;

public class CDPBasic implements ICDPParser {

    private ReadOnlyDatabase database;
    private String table;

    public CDPBasic(String filePath) throws SQLException {
        this.database = new ReadOnlyDatabase(filePath);
        this.table = getSignalTable();
    }

    private String getSignalTable() throws SQLException {
        AtomicReference<String> match = new AtomicReference<>(null);

        String sql = "SELECT name FROM sqlite_master WHERE type='table'";

        database.query(sql, (rs) -> {
            String name = rs.getString("name");

            if (name.contains("SQL") && name.contains("Logger") && !name.contains("Info") && !name.contains("Events")) {
                match.set(name);
            }
        });

        if (match.get() == null) {
            throw new RuntimeException("Database " + database.getURL() + " does not contain table SQLSignalLogger");
        }

        return match.get();
    }

    private long getFrom() throws SQLException {
        AtomicLong from = new AtomicLong(-1);

        String sql = new SQL()
                .pattern("SELECT timestamp FROM @table ORDER BY timestamp ASC LIMIT 1")
                .field("table", table)
                .build();

        database.query(sql, (rs) -> {
            from.set((long) (rs.getDouble("timestamp") * 1000));
        });

        return from.get();
    }

    private long getTo() throws SQLException {
        AtomicLong to = new AtomicLong(-1);

        String sql = new SQL()
                .pattern("SELECT timestamp FROM @table ORDER BY timestamp DESC LIMIT 1")
                .field("table", table)
                .build();

        database.query(sql, (rs) -> {
            to.set((long) (rs.getDouble("timestamp") * 1000));
        });

        return to.get();
    }

    @Override
    public Long[] getBounds() throws SQLException {
        long from = getFrom();
        long to = getTo();


        if (from == -1 || to == -1) {
            throw new RuntimeException("Could not retrive bounds from: " + database.getURL());
        }

        return new Long[] { from, to };
    }

    @Override
    public HashMap<Long, Double> getRange(String signal, long from, long to) throws Exception {
        try {
            HashMap<Long, Double> values = new HashMap<>();

            String sql = new SQL()
                    .pattern("SELECT timestamp, @signal AS value FROM @table WHERE timestamp BETWEEN @from AND @to")
                    .field("signal", signal)
                    .field("table", table)
                    .field("from", (double) from / 1000)
                    .field("to", (double) to / 1000)
                    .build();

            database.query(sql, (rs) -> {
                long timestamp = (long) (rs.getDouble("timestamp") * 1000);
                double value = rs.getDouble("value");
                values.put(timestamp, value);
            });

            return values;
        } catch (Exception ex) {
            String message = "Failed to getRange for signal " + signal
                    + "\nin database " + database.getURL()
                    + "\nin table " + table
                    + "\nin range " + from + " => " + to;
            throw new Exception(message, ex);
        }
    }
}
