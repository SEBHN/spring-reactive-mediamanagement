package de.hhn.mvs.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OktaCredentials {
    private Map<String, String> password;

    public OktaCredentials(){
        password = new HashMap<>();
    }

    public OktaCredentials(String plainTextPassword){
        this();
        setPassword(plainTextPassword);
    }

    public Map<String, String> getPassword() {
        return password;
    }

    public void setPassword(Map<String, String> password){
        this.password = password;
    }

    public void setPassword(String plainTextPassword) {
        this.password.put("value", plainTextPassword);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OktaCredentials that = (OktaCredentials) o;
        return Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(password);
    }

}
