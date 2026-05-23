package com.inkit.backend.hearing;

import java.time.LocalDateTime;
import java.util.UUID;

import com.inkit.backend.case_mgmt.Case;
import com.inkit.backend.firm.Firm;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hearings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hearing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDateTime hearingDate;
    private String status;
    private String courtroom;
    private String hearingType;
    private String notes;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @ManyToOne
    private Case caseRef;

    @ManyToOne
    private Firm firm;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdDate = now;
        updatedDate = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
