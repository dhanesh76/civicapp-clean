package com.visioners.civic.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.exception.UserNotFoundException;
import com.visioners.civic.user.entity.Users;
import com.visioners.civic.user.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    private final UsersRepository usersRepository;
    
    @Override
    public UserDetails loadUserByUsername(String loginId) {
        Users user = usersRepository.findByMobileNumberOrEmail(loginId, loginId)
            .orElseThrow(() -> new UserNotFoundException("No user exist with " + loginId));
        return new UserPrincipal(user);
    }
}

