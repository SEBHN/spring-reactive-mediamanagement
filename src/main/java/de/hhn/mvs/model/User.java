package de.hhn.mvs.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

@JsonDeserialize(as = UserImpl.class)
public interface User extends UserDetails {

    String getId();

    String getEmail();

    void setEmail(String email);

    String getPassword();

    void setPassword(String password);

    String getToken();

    void setToken(String token);

    Set<String> getRoles();

    void setRoles(Set<String> roles);

    void addRole(String role);

    void addRoles(Set<String> roles);
}
