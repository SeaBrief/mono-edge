package com.seabrief.Logger.Models;

public class Metadata {
    private int id;
    private String name;
    private String type;
    private String path;
    private String description;

    public Metadata(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Metadata(int id, String name, String type, String path) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.path = path;
    }

    public Metadata(int id, String name, String path) {
        this.id = id;
        this.name = name;
        this.path = path;
    }

    public Metadata(String name, String path, String description, int id) {
        this.name = name;
        this.path = path;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }

}
