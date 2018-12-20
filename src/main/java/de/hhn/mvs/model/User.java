package de.hhn.mvs.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@JsonDeserialize(as = UserImpl.class)
public interface User extends UserDetails {

    String getId();

    String getEmail();

    void setEmail(String email);

    String getPassword();

    void setPassword(String password);

    List<String> getRoles();

    void setRoles(List<String> roles);



}
