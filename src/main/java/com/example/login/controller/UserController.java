package com.example.login.controller;

import com.example.login.exception.ResourceNotFoundException;
import com.example.login.model.UserProfile;
import com.example.login.repository.UserRepository;
import com.example.login.security.CurrentUser;
import com.example.login.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public UserProfile getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "id", userPrincipal.getId()));
    }
}
