package com.inkit.backend.auth.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSummaryResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String accountType;
    private String firmName;
    private String barNumber;
    private String phone;
    private String address;
    private String specialization;
    private Boolean isActive;
    private LocalDateTime createdDate;
}
