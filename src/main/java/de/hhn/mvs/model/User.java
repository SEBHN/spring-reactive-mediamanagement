package de.hhn.mvs.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = UserImpl.class)
public interface User {

    String getId();

    boolean isAdmin();

    String getEmail();

    void setEmail(String email);

    String getHashedPassword();

    void setHashedPassword(String alreadyHashedPassword);

    void setPassword(String passwordToHash);

    String getToken();

    void setToken(String token);

    User copy();
}
