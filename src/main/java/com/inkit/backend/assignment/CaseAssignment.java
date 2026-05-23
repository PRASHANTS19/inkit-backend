package com.inkit.backend.assignment;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "case_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID caseId;
    private UUID assignedToUserId;
    private UUID assignedByUserId;
    private LocalDateTime assignmentDate;
    private String roleInCase;
    @Column(length = 5000)
    private String permissionsJson;
    @Column(length = 2000)
    private String notes;
    private UUID firmId;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdDate = now;
        updatedDate = now;
        if (assignmentDate == null) {
            assignmentDate = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
