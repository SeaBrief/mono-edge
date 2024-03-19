package com.seabrief.Logger.Parser.Extractors.XML;

import org.xml.sax.helpers.DefaultHandler;

import com.seabrief.Logger.Models.SignalMetadata;
import com.seabrief.Services.Tools.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;

public class XMLMetadataParser extends DefaultHandler {
    private SAXParserFactory factory;
    private FileInputStream inputStream;
    private HashMap<String, SignalMetadata> signals = new HashMap<>();
    private String databaseName;
    private String targetFile;

    public XMLMetadataParser(String targetFile) {
        this.factory = SAXParserFactory.newInstance();
        this.inputStream = null;
        this.targetFile = targetFile;
    }

    public HashMap<String, SignalMetadata> getSignals() {
        return this.signals;
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        try {
            if (qName.equalsIgnoreCase("SignalSource") || qName.equalsIgnoreCase("LoggedValue")) {
                String name = attributes.getValue("Name");
                String routing = attributes.getValue("Routing");
                String description = attributes.getValue("Description");

                if (routing == null) {
                    // For Old Compact Datastore, Path is attribute for routing
                    routing = attributes.getValue("Path");
                }
               
                if (routing != null) {
                    this.signals.put(name, new SignalMetadata(name, routing, description));
                }
            }
    
            if (qName.equalsIgnoreCase("Database") || qName.equalsIgnoreCase("Datastore")) {
                String src = attributes.getValue("DBName");
    
                if (src == null) {
                    src = attributes.getValue("Name");
                }
    
                if (src == null) {
                   return;
                }
    
                if (src.contains("..") || !src.startsWith("/")) {
                    String root = this.targetFile.substring(0, this.targetFile.lastIndexOf("Components"));
                    this.databaseName = Paths.get(root, src).normalize().toString();
                } else {
                    this.databaseName = src;
                }
    
            }
        } catch (Exception e) {
            Logger.error("Error Parsing Line: " + uri + " \n" + e.getMessage());
        }
       
    }

    public XMLMetadataParser parse() throws IOException {
        try {
            inputStream = new FileInputStream(this.targetFile);
            FileChannel channel = inputStream.getChannel();
            long fileSize = channel.size();
            channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
            SAXParser parser = factory.newSAXParser();
            parser.parse(inputStream, this);
        } catch (Exception e) {
            Logger.error("Error Metadata parsing XML file: " + this.targetFile + " " + e.getMessage());
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return this;
    }
}
