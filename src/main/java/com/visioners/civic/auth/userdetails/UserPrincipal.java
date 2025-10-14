package com.visioners.civic.auth.userdetails;

import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.visioners.civic.user.entity.Users;

public class UserPrincipal implements UserDetails{

    private final Users user;

    public UserPrincipal(Users user){
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections
            .singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())
            );
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getMobileNumber();
    }

    public Users getUser(){
        return user;
    }
}
