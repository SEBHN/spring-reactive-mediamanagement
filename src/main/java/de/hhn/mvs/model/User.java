package de.hhn.mvs.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.security.NoSuchAlgorithmException;

@JsonDeserialize(as = UserImpl.class)
public interface User {

    String getId();

    boolean isAdmin();

    String getEmail();

    void setEmail(String email);

    String getPassword();

    void setPassword(String password);

    String getToken();

    void setToken(String token);

    void hashPassword();

}
