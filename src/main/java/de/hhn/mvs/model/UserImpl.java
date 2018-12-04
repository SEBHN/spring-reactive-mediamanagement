package de.hhn.mvs.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

@Document
public class UserImpl implements User{

    @Id
    private String id;

    @Indexed(unique=true)
    private String email;

    private String password;
    private String token;

    private Set<String> authorities = new HashSet<>();


    public UserImpl(){
    }

    public UserImpl(String id, String email, String password, String token){
        this.id = id;
        this.email = email;
        this.password = password;
        this.token = token;
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
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public Set<String> getRoles() {
        return authorities;
    }


    @Override
    public void setRoles(Set<String> roles) {
        this.authorities.clear();
        this.authorities.addAll(roles);
    }

    @Override
    public void addRole(String role) {
        this.authorities.add(role);
    }

    @Override
    public void addRoles(Set<String> roles) {
        this.authorities.addAll(roles);
    }

    @Override
    public String toString(){
        return "User{" +
                "id='" + id + "\'" +
                "email='" + email + "\'" +
                "password='" + password + "\'" +
                "token='" + token + "\'" +
                "}";
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserImpl user = (UserImpl) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(email, user.email) &&
                Objects.equals(password, user.password) &&
                Objects.equals(token, user.token);
    }

    @Override
    public int hashCode(){
        return Objects.hash(id, email, password, token);
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> tmp = new ArrayList<GrantedAuthority>();
        for (String role : authorities) {
            tmp.add(new SimpleGrantedAuthority(role));
        }
        return tmp;
    }

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

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
