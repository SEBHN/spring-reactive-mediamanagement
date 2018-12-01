package de.hhn.mvs.security;

import daggerok.users.User;
import de.hhn.mvs.model.User;
import de.hhn.mvs.model.UserImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

// 2
class CustomUserDetails extends UserImpl implements UserDetails {


    private static final long serialVersionUID = -5306760386322252897L;

    public CustomUserDetails(final User user) {

        this.setId(user.getId())
                .setUsername(user.getUsername())
                .setPassword(user.getPassword())
                .setLastModifiedAt(user.getLastModifiedAt());
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList("ROLE_USER");
    }

    @Override public boolean isAccountNonExpired() {
        return isEnabled();
    }

    @Override public boolean isAccountNonLocked() {
        return isEnabled();
    }

    @Override public boolean isCredentialsNonExpired() {
        return isEnabled();
    }

    @Override public boolean isEnabled() {
        return true;
    }


    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean isAdmin() {
        return false;
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public void setEmail(String email) {

    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public void setPassword(String password) {

    }

    @Override
    public String getToken() {
        return null;
    }

    @Override
    public void setToken(String token) {

    }

    @Override
    public String getUsername() {
        return getEmail();
    }
}