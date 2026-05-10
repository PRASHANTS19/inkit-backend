package com.inkit.backend.invitation;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inkit.backend.invitation.dto.InvitationFilterRequest;
import com.inkit.backend.invitation.dto.InvitationResponse;
import com.inkit.backend.invitation.dto.InvitationUpsertRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @GetMapping
    public List<InvitationResponse> listInvitations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String status) {
        return invitationService.listInvitations(userDetails.getUsername(), status);
    }

    @PostMapping("/filter")
    public List<InvitationResponse> filterInvitations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody InvitationFilterRequest request) {
        return invitationService.filterInvitations(userDetails.getUsername(), request);
    }

    @GetMapping("/{id}")
    public InvitationResponse getInvitation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return invitationService.getById(userDetails.getUsername(), id);
    }

    @PostMapping
    public InvitationResponse createInvitation(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody InvitationUpsertRequest request) {
        return invitationService.create(userDetails.getUsername(), request);
    }

    @PutMapping("/{id}")
    public InvitationResponse updateInvitation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestBody InvitationUpsertRequest request) {
        return invitationService.update(userDetails.getUsername(), id, request);
    }

    @PutMapping("/{id}/accept")
    public String acceptInvitation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        invitationService.accept(userDetails.getUsername(), id);
        return "Invitation accepted";
    }

    @PutMapping("/{id}/decline")
    public String declineInvitation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        invitationService.decline(userDetails.getUsername(), id);
        return "Invitation declined";
    }

    @DeleteMapping("/{id}")
    public String deleteInvitation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        invitationService.delete(userDetails.getUsername(), id);
        return "Invitation deleted";
    }
}

