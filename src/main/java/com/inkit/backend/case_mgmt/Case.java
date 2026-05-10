package com.inkit.backend.case_mgmt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.inkit.backend.auth.User;
import com.inkit.backend.client.Client;
import com.inkit.backend.common.enums.CaseType;
import com.inkit.backend.common.enums.Court;
import com.inkit.backend.common.enums.Priority;
import com.inkit.backend.common.enums.Status;
import com.inkit.backend.firm.Firm;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Case {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String caseTitle;
    private String caseNumber;
    private String clientName;
    private String clientContact;

    @Enumerated(EnumType.STRING)
    private Court court;

    @Enumerated(EnumType.STRING)
    private CaseType caseType;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private LocalDate nextHearingDate;
    private String caseDescription;
    private String opposingCounsel;
    private Double caseValue;
    private LocalDate filingDate;
    private String tags;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @ManyToOne
    private User creator;

    @ManyToOne
    private User assignedTo;

    @ManyToOne
    private Client client;

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
