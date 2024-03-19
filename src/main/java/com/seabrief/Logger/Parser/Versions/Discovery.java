package com.seabrief.Logger.Parser.Versions;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.seabrief.Logger.Models.CDPDataStore;
import com.seabrief.Logger.Parser.Extractors.SQL.ReadOnlyDatabase;
import com.seabrief.Services.Tools.Logger;


public class Discovery {
    public static CDPDataStore getDatabaseType(String file) throws SQLException {
        List<String> tables = getDatabaseTables(file);

        String searchString = String.join(".", tables);

        if (searchString.contains("ConnectionNodeMap")) {
            Logger.log("Discovered CDPDatastore [SPLIT]: " + file);
            return CDPDataStore.SPLIT;
        }
        if (searchString.contains("SignalMap")) {
            Logger.log("Discovered CDPDatastore [COMPACT]: " + file);
            return CDPDataStore.COMPACT;
        }
        if (searchString.contains("SQL") && searchString.contains("Logger")) {
            Logger.log("Discovered CDPDatastore [BASIC]: " + file);
            return CDPDataStore.BASIC;
        }

        return CDPDataStore.UNKNOWN;
    }

    private static List<String> getDatabaseTables(String path) throws SQLException {
        ReadOnlyDatabase database = new ReadOnlyDatabase(path);
        List<String> tables = new ArrayList<>();

        String sql = "SELECT name FROM sqlite_master WHERE type='table'";

        database.query(sql, (rs) -> {
            tables.add(rs.getString("name"));
        });

        return tables;
    }
}
