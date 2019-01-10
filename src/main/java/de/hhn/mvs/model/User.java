package de.hhn.mvs.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = UserImpl.class)
public interface User {

    String getId();

    boolean isAdmin();

    String getEmail();

    void setEmail(String email);

    String getName();

    void setName(String name);

    String getPassword();

    void setPassword(String password);

    String getToken();

    void setToken(String token);
}
