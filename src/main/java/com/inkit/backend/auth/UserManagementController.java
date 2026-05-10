package com.inkit.backend.auth;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inkit.backend.auth.dto.UpdateManagedUserRequest;
import com.inkit.backend.auth.dto.UserFilterRequest;
import com.inkit.backend.auth.dto.UserSummaryResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping
    public List<UserSummaryResponse> listUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, name = "account_type") String accountType,
            @RequestParam(required = false, name = "firm_admin_id") String firmAdminId) {
        return userManagementService.listUsers(userDetails.getUsername(), accountType, firmAdminId);
    }

    @PostMapping("/filter")
    public List<UserSummaryResponse> filterUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserFilterRequest request) {
        return userManagementService.filterUsers(userDetails.getUsername(), request);
    }

    @GetMapping("/{id}")
    public UserSummaryResponse getUserById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return userManagementService.getUserById(userDetails.getUsername(), id);
    }

    @PutMapping("/{id}")
    public UserSummaryResponse updateUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestBody UpdateManagedUserRequest request) {
        return userManagementService.updateUser(userDetails.getUsername(), id, request);
    }
}
