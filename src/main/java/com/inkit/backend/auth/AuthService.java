package com.inkit.backend.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.inkit.backend.auth.dto.LoginRequest;
import com.inkit.backend.auth.dto.RegisterRequest;
import com.inkit.backend.auth.dto.UpdateUserRequest;
import com.inkit.backend.common.enums.Role;
import com.inkit.backend.config.security.*;
import com.inkit.backend.firm.Firm;
import com.inkit.backend.firm.FirmRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FirmRepository firmRepository;

    public String login(LoginRequest request) {

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        System.out.println("Login attempt for: " + request.getEmail());            

        return jwtUtil.generateToken(request.getEmail());
    }

    public void register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        // 🔹 create default firm
        String firmName = request.getEmail().split("@")[0] + " Firm";

        Firm firm = Firm.builder()
                .name(firmName)
                .build();

        firm = firmRepository.save(firm);

        // 🔹 create user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.FIRM_ADMIN)
                .isActive(true)
                .firm(firm)
                .build();

        userRepository.save(user);
    }

    public void updateUser(String email, UpdateUserRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        user.setName(request.getName());
        user.setBarRegistrationNumber(request.getBarRegistrationNumber());
        user.setYOE(request.getYOE());
        user.setAreaOfSpecialization(request.getAreaOfSpecialization());
        user.setStreetAddress(request.getStreetAddress());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setPinCode(request.getPinCode());

        userRepository.save(user);
    }

}
