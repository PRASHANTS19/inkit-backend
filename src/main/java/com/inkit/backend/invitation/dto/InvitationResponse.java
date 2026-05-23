package com.inkit.backend.invitation.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InvitationResponse {
    private UUID id;
    @JsonProperty("inviter_id")
    private UUID inviterId;
    @JsonProperty("inviter_name")
    private String inviterName;
    @JsonProperty("invitee_email")
    private String inviteeEmail;
    private String status;
    private String role;
    @JsonProperty("firm_name")
    private String firmName;
    @JsonProperty("accepted_at")
    private LocalDateTime acceptedAt;
    @JsonProperty("created_date")
    private LocalDateTime createdDate;
    @JsonProperty("updated_date")
    private LocalDateTime updatedDate;
}

