package com.inkit.backend.invitation;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InvitationRepository extends JpaRepository<Invitation, UUID>, JpaSpecificationExecutor<Invitation> {
}

