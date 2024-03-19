package com.seabrief;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import com.seabrief.Logger.Models.CDPDataStore;
import com.seabrief.Logger.Parser.Versions.CDPBasic;
import com.seabrief.Logger.Parser.Versions.CDPCompact;
import com.seabrief.Logger.Parser.Versions.CDPSplit;
import com.seabrief.Logger.Parser.Versions.Discovery;

public class DatabaseTest {
    public static String split_path = "./assets/4.x/Split/SignalLog.db";
    public static String compact_path = "./assets/4.x/Compact/SignalLog.db";
    public static String basic_path = "./assets/3.x/Logs/SignalLog.db";

    @Test
    public void DiscoveryTest() {
        try {
            CDPDataStore basic = Discovery.getDatabaseType(basic_path);
            CDPDataStore compact = Discovery.getDatabaseType(compact_path);
            CDPDataStore split = Discovery.getDatabaseType(split_path);

            if (basic != CDPDataStore.BASIC) {
                throw new Exception("Failed to recognize Basic Datastore");
            }

            if (compact != CDPDataStore.COMPACT) {
                throw new Exception("Failed to recognize Basic Datastore");
            }

            if (split != CDPDataStore.SPLIT) {
                throw new Exception("Failed to recognize Basic Datastore");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void BasicTest() {
        try {
            CDPBasic basic = new CDPBasic(basic_path);

            Long[] bounds = basic.getBounds();

            HashMap<Long, Double> values = basic.getRange("CTRL1_CPU", bounds[1] - 2 * 60 * 1000, bounds[1]);

            System.out.println("Basic range search returned " + values.size() + " values");
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void CompactTest() {
        try {
            CDPCompact basic = new CDPCompact(compact_path);

            Long[] bounds = basic.getBounds();

            HashMap<Long, Double> values = basic.getRange("CPULoad", bounds[1] - 2 * 60 * 1000, bounds[1]);

            System.out.println("Compact range search returned " + values.size() + " values");
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void SplitTest() {
        try {
            CDPSplit basic = new CDPSplit(split_path);

            Long[] bounds = basic.getBounds();

            HashMap<Long, Double> values = basic.getRange("CTRL_CPULoad", bounds[1] - 2 * 60 * 1000, bounds[1]);

            System.out.println("Split range search returned " + values.size() + " values");
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }
}
