package com.inkit.backend.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.inkit.backend.auth.User;
import com.inkit.backend.case_mgmt.Case;
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
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;
    private String description;
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String taskType;
    private Double estimatedHours;
    private Double actualHours;
    private String notes;
    private Boolean reminderSent;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @ManyToOne
    private Case caseRef;

    @ManyToOne
    private User assignedTo;

    @ManyToOne
    private Firm firm;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdDate = now;
        updatedDate = now;
        if (status == null) {
            status = Status.PENDING;
        }
        if (priority == null) {
            priority = Priority.MEDIUM;
        }
        if (reminderSent == null) {
            reminderSent = false;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
