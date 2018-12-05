package de.hhn.mvs.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document
public final class UserImpl implements User{
    @Id
    private String id;
    private boolean admin;
    private String email;
    private String password;
    private String token;

    public UserImpl(){

    }

    public UserImpl(String id, boolean admin, String email, String password, String token){
        this.id = id;
        this.admin = admin;
        this.email = email;
        this.password = password;
        this.token = token;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isAdmin() {
        return admin;
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
    public String toString(){
        return "User{" +
                "id='" + id + "\'" +
                "admin='" + admin + "\'" +
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
                Objects.equals(admin, user.admin) &&
                Objects.equals(email, user.email) &&
                Objects.equals(password, user.password) &&
                Objects.equals(token, user.token);
    }

    @Override
    public int hashCode(){
        return Objects.hash(id, admin, email, password, token);
    }

}