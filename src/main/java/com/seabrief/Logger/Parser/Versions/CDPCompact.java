package com.seabrief.Logger.Parser.Versions;

import java.sql.SQLException;
import java.util.HashMap;

import com.seabrief.Logger.Models.CDPDataStore;
import com.seabrief.Logger.Parser.Extractors.Blob.Unpacker;

public class CDPCompact implements ICDPParser {

    private Unpacker unpacker;

    public CDPCompact(String filePath) throws SQLException {
        this.unpacker = new Unpacker(filePath, CDPDataStore.COMPACT);
    }

    @Override
    public Long[] getBounds() throws SQLException {
        return unpacker.getBounds();
    }

    @Override
    public HashMap<Long, Double> getRange(String signal, long from, long to) throws Exception {
        return unpacker.getRange(signal, from, to);
    }
}
