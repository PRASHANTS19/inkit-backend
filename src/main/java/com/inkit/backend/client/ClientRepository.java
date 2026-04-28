package com.inkit.backend.client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {
    Optional<Client> findByEmail(String email);
    List<Client> findByUsersId(UUID userId);
    boolean existsByIdAndUsersId(UUID clientId, UUID userId);
}
