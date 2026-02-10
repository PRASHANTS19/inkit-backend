package com.inkit.backend.firm;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface FirmRepository extends JpaRepository<Firm, UUID> {
    Optional<Firm> findById(UUID id);
} 