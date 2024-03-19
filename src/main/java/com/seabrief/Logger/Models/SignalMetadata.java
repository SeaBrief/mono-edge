package com.seabrief.Logger.Models;

public class SignalMetadata {
    private String name;
    private String key;
    private String route;
    private String description;
    private String databasePath;

    public SignalMetadata(String name, String route, String description) {
        this.key = name;
        this.route = route;
        this.description = description;
    }

    public String getDatabasePath() {
        return this.databasePath;
    }

     public void setName(String name) {
        this.name = name;
    }

    public void setDatabasePath(String databasePath) {
        this.databasePath = databasePath;
    }

    public String getKey() {
        return this.key;
    }

    public String getName() {
        return this.name;
    }

    public String getRoute() {
        return this.route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getDescription() {
        return this.description;
    }
}
