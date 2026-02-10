package com.inkit.backend.membership;

import java.lang.StackWalker.Option;
import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, Long>  {
    List<Membership> findByUserId(UUID userId);
    List<Membership> findByFirmId(UUID firmId);
    Optional<Membership> findByUserIdAndFirmId(UUID userId, UUID firmId);
}
