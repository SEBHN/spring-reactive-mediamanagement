package de.hhn.mvs.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OktaUser {

    private Map<String, String> profile;
    private OktaCredentials credentials;

    public OktaUser(){
        profile = new HashMap<>();
        credentials = new OktaCredentials();
    }

    public static OktaUser create(User user){
        OktaUser oktaUser = new OktaUser();
        oktaUser.setEmail(user.getEmail());
        oktaUser.setFirstName(user.getName());
        oktaUser.setCredentials(new OktaCredentials(user.getPassword()));
        return oktaUser;
    }

    public Map<String, String> getProfile() {
        return profile;
    }

    public void setProfile(Map<String, String> profile) {
        this.profile = profile;
    }

    public void setFirstName(String name){
        profile.put("firstName", name);
    }

    public void setEmail(String email){
        profile.put("email", email);
        profile.put("login", email);
    }

    public void setCredentials(OktaCredentials credentials) {
        this.credentials = credentials;
    }

    public OktaCredentials getCredentials() {
        return credentials;
    }

    @Override
    public String toString() {
        return "OktaUser{" +
                "profile=" + profile +
                ", credentials=" + credentials +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OktaUser oktaUser = (OktaUser) o;
        return Objects.equals(profile, oktaUser.profile) &&
                Objects.equals(credentials, oktaUser.credentials);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profile, credentials);
    }
}
