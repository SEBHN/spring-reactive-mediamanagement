package de.hhn.mvs.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;



@Document
public final class UserImpl implements User {

    @Id
    private String id;
    @Indexed(unique=true)
    private String email;

    private boolean admin;
    private String token;
    private String name;
    private String password;
    private List<String> roles = new ArrayList<>();

    public UserImpl(){
    }

    public UserImpl(String id, boolean admin, String email, String password, String token, String name, List<String> roles) {

        this.id = id;
        this.email = email;
        this.password = password;
        this.roles.addAll(roles);
        //TODO: maybe not nice but well
        //if(!this.roles.contains("ROLE_USER")) roles.add("ROLE_USER");
        this.admin = admin;
        this.password = password;
        this.token = token;
        this.name = name;

    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean isAdmin() {
        return admin;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public List<String> getRoles() {
        return roles;
    }

    @Override
    public void setRoles(List<String> role) {
        this.roles.clear();
        this.roles.addAll(role);


    }


    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> tmp = new ArrayList<>();
        for (String role : roles) {
            tmp.add(new SimpleGrantedAuthority(role));
        }
        return tmp;

    }

    /**
     * Where to find the Username for SecurityContext
     * @return String (username as email)
     */
    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return "UserImpl{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", token='" + token + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", roles=" + roles +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserImpl user = (UserImpl) o;
        return id.equals(user.id) &&
                email.equals(user.email) &&
                Objects.equals(token, user.token) &&
                name.equals(user.name) &&
                password.equals(user.password) &&
                roles.equals(user.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, token, name, password, roles);
    }
}
