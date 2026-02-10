package com.inkit.backend.membership;


import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.inkit.backend.membership.DTO.CreateMembershipRequest;

@RestController
@RequestMapping("/api/memberships")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @PostMapping
    public Membership createMembership(@RequestBody CreateMembershipRequest request) {
        return membershipService.createMembership(
                request.userId,
                request.firmId,
                request.role
        );
    }

    @GetMapping("/user/{userId}")
    public List<Membership> getUserMemberships(@PathVariable UUID userId) {
        return membershipService.getUserMemberships(userId);
    }

    @GetMapping("/firm/{firmId}")
    public List<Membership> getFirmMembers(@PathVariable UUID firmId) {
        return membershipService.getFirmMembers(firmId);
    }
}