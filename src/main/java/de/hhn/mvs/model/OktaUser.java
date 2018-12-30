package de.hhn.mvs.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OktaUser {
    private Map<String, String> profile;
    private OktaCredentials credentials;
    private List<String> groupIds;


    public OktaUser(){
        profile = new HashMap<>();
        credentials = new OktaCredentials();
        groupIds = new ArrayList<>(1);
    }

    public static OktaUser create(User user, List<String> groupIds){
        OktaUser oktaUser = new OktaUser();
        oktaUser.setEmail(user.getEmail());
        oktaUser.setFirstName(user.getName());
        oktaUser.setCredentials(new OktaCredentials(user.getPassword()));
        oktaUser.setGroupIds(groupIds);
        return oktaUser;
    }

    public List<String> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<String> groupIds) {
        this.groupIds = groupIds;
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
                ", groupIds=" + groupIds +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OktaUser oktaUser = (OktaUser) o;
        return Objects.equals(profile, oktaUser.profile) &&
                Objects.equals(credentials, oktaUser.credentials) &&
                Objects.equals(groupIds, oktaUser.groupIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profile, credentials, groupIds);
    }
}
