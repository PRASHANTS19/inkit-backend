package com.inkit.backend.assignment;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CaseAssignmentRepository extends JpaRepository<CaseAssignment, UUID>, JpaSpecificationExecutor<CaseAssignment> {
}

