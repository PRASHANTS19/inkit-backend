package com.inkit.backend.invoice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.inkit.backend.auth.User;
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
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String invoiceNumber;
    private String status;
    private Double amount;
    private String notes;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @ManyToOne
    private Case caseRef;

    @ManyToOne
    private User createdBy;

    @ManyToOne
    private Firm firm;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdDate = now;
        updatedDate = now;
        if (status == null || status.isBlank()) {
            status = "draft";
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}

