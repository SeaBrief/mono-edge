package com.seabrief.Logger.Parser.Versions;

import java.sql.SQLException;
import java.util.HashMap;

public interface ICDPParser {
    Long[] getBounds() throws SQLException;
    HashMap<Long, Double> getRange(String signal, long from, long to) throws Exception;
}
