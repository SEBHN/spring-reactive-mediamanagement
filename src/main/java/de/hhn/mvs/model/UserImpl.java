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

@Document
public final class UserImpl implements User {

    @Id
    private String id;

    @Indexed(unique=true)
    private String email;

    private String password;
    private List<String> roles = new ArrayList<>();

    public UserImpl(){
    }

    public UserImpl(String id, String email, String raw_password, List<String> roles) {
        //PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        this.id = id;
        this.email = email;
        this.password = raw_password;
        this.roles.addAll(roles);
        //TODO: maybe not nice but well
        //if(!this.roles.contains("ROLE_USER")) roles.add("ROLE_USER");
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
    public List<String> getRoles() {
        return roles;
    }

    @Override
    public void setRoles(List<String> role) {
//        this.roles.clear();
//        this.roles.addAll(roles);
            this.roles = role;

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
                ", password='" + password + '\'' +
                ", roles='" + roles + '\'' +
                '}';
    }




}
