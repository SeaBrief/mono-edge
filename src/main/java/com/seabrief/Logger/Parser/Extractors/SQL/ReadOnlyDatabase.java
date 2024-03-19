package com.seabrief.Logger.Parser.Extractors.SQL;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class ReadOnlyDatabase {
    private final String url;

    public ReadOnlyDatabase(String filePath) throws SQLException {
        File file = new File(filePath);

        if (!file.exists()) {
            throw new SQLException("Database file not found: " +  filePath);
        }

        this.url = "jdbc:sqlite:" + filePath;
    }

    public String getURL() {
        return this.url;
    }

    public void query(String sql, RowCallback callback) throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("readOnly", "true");
        try (Connection conn = DriverManager.getConnection(url, properties);
                Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    callback.exec(rs);
                }
            }
        }
    }

    public interface RowCallback {
        void exec(ResultSet rs) throws SQLException;
    }
}
