package com.inkit.backend.invitation;

import java.time.LocalDateTime;
import java.util.UUID;

import com.inkit.backend.auth.User;

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
@Table(name = "invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private User inviter;

    private String inviteeEmail;
    private String status;
    private String role;
    private String firmName;
    private LocalDateTime acceptedAt;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdDate = now;
        updatedDate = now;
        if (status == null || status.isBlank()) {
            status = "pending";
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}

