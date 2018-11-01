package de.hhn.mvs.model;

public class Tag {
    private String id;
    private String name;

    public Tag() {
        // for jackson
    }

    public Tag(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
