package com.seabrief.Monitor.Parsers;

public class CDPApplication {
    public String name;
    public String version;
    public String address;


    public CDPApplication(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public CDPApplication(String name) {
        this.name = name;
    }
}
