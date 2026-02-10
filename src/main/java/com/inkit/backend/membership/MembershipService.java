package com.inkit.backend.membership;

import java.lang.reflect.Member;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.inkit.backend.auth.User;
import com.inkit.backend.auth.UserRepository;
import com.inkit.backend.common.enums.Role;
import com.inkit.backend.firm.Firm;
import com.inkit.backend.firm.FirmRepository;

@Service
public class MembershipService {

    @Autowired
    private MembershipRepository membershipRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FirmRepository firmRepository;

    public Membership createMembership(UUID userId, UUID firmId, Role role) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Firm firm = firmRepository.findById(firmId)
                .orElseThrow(() -> new RuntimeException("Firm not found"));

        Membership membership = new Membership(firmId, user, firm, role, null, null);

        return membershipRepository.save(membership);
    }

    public List<Membership> getUserMemberships(UUID userId) {
        return membershipRepository.findByUserId(userId);
    }

    public List<Membership> getFirmMembers(UUID firmId) {
        return membershipRepository.findByFirmId(firmId);
    }
}
