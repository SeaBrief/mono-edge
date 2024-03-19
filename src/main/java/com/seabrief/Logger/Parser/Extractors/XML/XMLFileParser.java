package com.seabrief.Logger.Parser.Extractors.XML;

import org.xml.sax.helpers.DefaultHandler;

import com.seabrief.Services.Tools.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;

public class XMLFileParser extends DefaultHandler {
    private Set<String> subComponents = new HashSet<>();
    private SAXParserFactory factory;
    private FileInputStream inputStream;
    private String targetFile;

    public XMLFileParser(String targetFile) {
        this.factory = SAXParserFactory.newInstance();
        this.inputStream = null;
        this.targetFile = targetFile;
    }

    public Set<String> getSubComponents() {
        return this.subComponents;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        try {
            if (qName.equalsIgnoreCase("SubComponent")) {
                String root = this.targetFile.substring(0, this.targetFile.lastIndexOf("Components"));
                String src = attributes.getValue("src");
                this.subComponents.add(Paths.get(root, src).toString().replaceAll("\\\\", "/"));
            }
        } catch (Exception e) {
            Logger.error("Error Parsing Line: " + uri + " \n" + e.getMessage());
        }
    }

    public XMLFileParser parse() throws IOException {
        try {
            inputStream = new FileInputStream(this.targetFile);
            FileChannel channel = inputStream.getChannel();
            long fileSize = channel.size();
            channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
            SAXParser parser = factory.newSAXParser();
            parser.parse(inputStream, this);
        } catch (Exception e) {
            Logger.error("Error File parsing XML file: " + this.targetFile + " " + e.getMessage());
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        if (this.subComponents.isEmpty()) {
            this.subComponents.add(this.targetFile);
        }

        return this;
    }
}