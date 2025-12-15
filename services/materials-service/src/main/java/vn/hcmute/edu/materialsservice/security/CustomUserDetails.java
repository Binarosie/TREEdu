package vn.hcmute.edu.materialsservice.security;

import vn.hcmute.edu.materialsservice.Model.Admin;
import vn.hcmute.edu.materialsservice.Model.Member;
import vn.hcmute.edu.materialsservice.Model.Supporter;
import vn.hcmute.edu.materialsservice.Model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user instanceof Admin) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else if (user instanceof Supporter) {
            return List.of(new SimpleGrantedAuthority("ROLE_SUPPORTER"));
        } else if (user instanceof Member) {
            return List.of(new SimpleGrantedAuthority("ROLE_MEMBER"));
        }
        return List.of();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.isActive();
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.isActive();
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    public User getUser() {
        return user;
    }
}