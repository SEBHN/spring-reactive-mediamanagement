package de.hhn.mvs.model;

import java.util.Objects;

public class Subfolder {
    private String name;

    public Subfolder() {
        // for jackson
    }


    public Subfolder(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Subfolder: " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subfolder folder = (Subfolder) o;
        return Objects.equals(name, folder.getName() );
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
