package com.seabrief;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.seabrief.Monitor.Parsers.CDPExtractor;
import com.seabrief.Monitor.Parsers.OSRelease;
import com.seabrief.Monitor.Parsers.OSReleaseExtractor;

public class MonitorTest 
{
    @Test
    public void CDPCheckTest4X() {
        try {
            CDPExtractor extractor = new CDPExtractor("./assets/4.x/messagelog.txt");

            extractor.extract();

            System.out.println("Found " + extractor.getApplications().size() + " Applications");

            assertTrue(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void CDPCheckTest3X() {
        try {
            CDPExtractor extractor = new CDPExtractor("./assets/3.x/messagelog.txt");

            extractor.extract();

            System.out.println("Found " + extractor.getApplications().size() + " Applications");
            assertTrue(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void OSReleaseTest() {
        try {
            OSRelease test = OSReleaseExtractor.extract("./assets/4.x/os-release");
            System.out.println("Found OS " + test.name + " " + test.version);

            assertTrue(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }
}
