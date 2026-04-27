package com.inkit.backend.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inkit.backend.auth.dto.AuthResponse;
import com.inkit.backend.auth.dto.LoginRequest;
import com.inkit.backend.auth.dto.RegisterRequest;
import com.inkit.backend.auth.dto.UpdateUserRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody LoginRequest request) {

        String token = authService.login(request);
        return AuthResponse.builder()
                .token(token)
                .build();
    }

    @PostMapping("/register")
    public String register(
            @RequestBody RegisterRequest request) {

        authService.register(request);
        return "User created successfully";
    }

    @PutMapping("/profile")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateUserRequest request) {

        authService.updateUser(userDetails.getUsername(), request);

        return "Profile updated";
    }

}
