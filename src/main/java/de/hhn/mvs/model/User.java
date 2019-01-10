package de.hhn.mvs.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@JsonDeserialize(as = UserImpl.class)
public interface User extends UserDetails {

    String getId();

    String getEmail();

    void setEmail(String email);

    String getName();

    void setName(String name);

    String getPassword();

    void setPassword(String password);

    boolean isAdmin();

    String getToken();

    List<String> getRoles();

    void setRoles(List<String> role);



}
