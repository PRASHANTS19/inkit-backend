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

        // Determine role from accountType field; default to INDEPENDENT if not provided
        Role role = resolveRole(request.getAccountType());

        User.UserBuilder userBuilder = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getFullName())
                .barRegistrationNumber(request.getBarNumber())
                .role(role)
                .isActive(true);

        // Associates register themselves but belong to no firm yet —
        // a law_firm_admin will invite/link them later.
        // For all other self-registering roles, create a default firm.
        if (role != Role.ASSOCIATE) {
            String firmName = (request.getFirmName() != null && !request.getFirmName().isBlank())
                    ? request.getFirmName()
                    : request.getEmail().split("@")[0] + " Firm";

            Firm firm = firmRepository.save(Firm.builder().name(firmName).build());
            userBuilder.firm(firm);
        }

        userRepository.save(userBuilder.build());
    }

    /**
     * Maps the frontend account_type string to the backend {@link Role} enum.
     * Defaults to {@link Role#INDEPENDENT} when accountType is null or blank.
     */
    private Role resolveRole(String accountType) {
        if (accountType == null || accountType.isBlank()) {
            return Role.INDEPENDENT;
        }
        return switch (accountType.trim().toLowerCase()) {
            case "law_firm_admin" -> Role.FIRM_ADMIN;
            case "associate" -> Role.ASSOCIATE;
            case "independent_advocate" -> Role.INDEPENDENT;
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid account_type: " + accountType);
        };
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
