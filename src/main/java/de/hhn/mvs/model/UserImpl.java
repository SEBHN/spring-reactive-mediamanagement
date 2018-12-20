package de.hhn.mvs.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

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


   private List<String> authorities = new ArrayList<>();

    public UserImpl(){
    }

    public UserImpl(String id, String email, String raw_password, List<String> authorities) {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        this.id = id;
        this.email = email;
        this.password = encoder.encode(raw_password);
        //this.authorities.addAll(authorities);
        //TODO: maybe not nice but well
       // if(!this.authorities.contains("ROLE_USER")) authorities.add("ROLE_USER");
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
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        this.password = encoder.encode(password);
    }

    @Override
    public List<String> getRoles() {
        return authorities;
    }

    @Override
    public void setRoles(List<String> roles) {
        this.authorities.clear();
        this.authorities.addAll(roles);
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> tmp = new ArrayList<>();
        for (String role : authorities) {
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
}
