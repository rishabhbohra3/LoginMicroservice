package com.example.login.controller;

import com.example.login.exception.BadRequestException;
import com.example.login.model.AuthProvider;
import com.example.login.model.UserProfile;
import com.example.login.payload.ApiResponse;
import com.example.login.payload.AuthResponse;
import com.example.login.payload.LoginRequest;
import com.example.login.payload.SignUpRequest;
import com.example.login.repository.UserRepository;
import com.example.login.security.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.createToken(authentication);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("Email address already in use.");
        }

        UserProfile userProfile = new UserProfile();
        userProfile.setName(signUpRequest.getName());
        userProfile.setEmail(signUpRequest.getEmail());
        userProfile.setPassword(signUpRequest.getPassword());
        userProfile.setProvider(AuthProvider.local);

        userProfile.setPassword(passwordEncoder.encode(userProfile.getPassword()));

        System.out.println(userProfile.getPassword());
        UserProfile result = userRepository.save(userProfile);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/userProfile/me")
                .buildAndExpand(result.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "UserProfile registered successfully@"));
    }

    @GetMapping(value = "/hello")
    public String get(){
        return "Hello";
    }

}
