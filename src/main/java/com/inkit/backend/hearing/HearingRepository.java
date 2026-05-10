package com.inkit.backend.hearing;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HearingRepository extends JpaRepository<Hearing, UUID>, JpaSpecificationExecutor<Hearing> {
}

