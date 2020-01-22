package com.example.login.security;


import com.example.login.exception.ResourceNotFoundException;
import com.example.login.model.UserProfile;
import com.example.login.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        UserProfile userProfile = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("UserProfile not found with email : " + email)
        );

        return UserPrincipal.create(userProfile);
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        UserProfile userProfile = userRepository.findById(id).orElseThrow(
            () -> new ResourceNotFoundException("UserProfile", "id", id)
        );

        return UserPrincipal.create(userProfile);
    }
}