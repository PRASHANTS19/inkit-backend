package com.inkit.backend.case_mgmt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inkit.backend.auth.User;
import com.inkit.backend.client.Client;
import com.inkit.backend.common.enums.CaseType;
import com.inkit.backend.common.enums.Court;
import com.inkit.backend.common.enums.Priority;
import com.inkit.backend.common.enums.Status;
import com.inkit.backend.firm.Firm;

import jakarta.persistence.Column;
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

    @JsonProperty("case_title")
    private String caseTitle;
    @JsonProperty("case_number")
    private String caseNumber;
    @JsonProperty("cnr_number")
    private String cnrNumber;
    @JsonProperty("registration_number")
    private String registrationNumber;
    @JsonProperty("filing_number")
    private String filingNumber;
    @JsonProperty("client_name")
    private String clientName;
    @JsonProperty("client_contact")
    private String clientContact;
    @JsonProperty("last_ecourt_sync")
    private LocalDateTime lastEcourtSync;
    @JsonProperty("ecourt_sync_status")
    private String ecourtSyncStatus;

    // eCourt API enriched fields
    @JsonProperty("ecourt_case_type")
    private String ecourtCaseType;       // raw API value e.g. "SLP_CRL"
    @JsonProperty("ecourt_case_status")
    private String ecourtCaseStatus;     // raw API value e.g. "HEARING"

    @Column(columnDefinition = "TEXT")
    private String judges;               // pipe-separated e.g. "K.V. VISWANATHAN|N.K. SINGH"

    @Column(columnDefinition = "TEXT")
    private String petitioners;          // pipe-separated

    @Column(columnDefinition = "TEXT")
    @JsonProperty("petitioner_advocates")
    private String petitionerAdvocates;  // pipe-separated

    @Column(columnDefinition = "TEXT")
    private String respondents;          // pipe-separated

    @Column(columnDefinition = "TEXT")
    @JsonProperty("respondent_advocates")
    private String respondentAdvocates;  // pipe-separated

    @Enumerated(EnumType.STRING)
    private Court court;

    @Enumerated(EnumType.STRING)
    @JsonProperty("case_type")
    private CaseType caseType;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @JsonProperty("next_hearing_date")
    private LocalDate nextHearingDate;
    @JsonProperty("case_description")
    private String caseDescription;
    @JsonProperty("opposing_counsel")
    private String opposingCounsel;
    @JsonProperty("case_value")
    private Double caseValue;
    @JsonProperty("filing_date")
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
