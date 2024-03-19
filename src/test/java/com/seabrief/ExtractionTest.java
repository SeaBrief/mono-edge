package com.seabrief;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import com.seabrief.Logger.Models.SignalMetadata;
import com.seabrief.Logger.Parser.Extractors.XML.XMLExtractor;

public class ExtractionTest 
{
    @Test
    public void XMLExtraction4X() {
        try {

            String root = "./assets/4.x/Components";
            XMLExtractor extractor = new XMLExtractor(root);

            HashMap<String, SignalMetadata> signals = extractor.extract();

            for (SignalMetadata metadata : signals.values()) {
             
                System.out.println(metadata.getName());
                System.out.println(metadata.getName() + " | " + metadata.getDescription() + " | " + metadata.getRoute());
            }

            assertTrue(true);
        } catch (Exception ex) {
            ex.printStackTrace();

            assertTrue(false);
        }
    }

    @Test
    public void XMLExtraction3X() {
        try {

            String root = "./assets/3.x/Components";
            XMLExtractor extractor = new XMLExtractor(root);

            HashMap<String, SignalMetadata> signals = extractor.extract();

            for (SignalMetadata metadata : signals.values()) {
             
                System.out.println(metadata.getName());
                System.out.println(metadata.getName() + " | " + metadata.getDescription() + " | " + metadata.getRoute());
            }

            assertTrue(true);
        } catch (Exception ex) {
            ex.printStackTrace();

            assertTrue(false);
        }
    }
}
