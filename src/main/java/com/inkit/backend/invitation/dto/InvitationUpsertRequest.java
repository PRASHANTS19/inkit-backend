package com.inkit.backend.invitation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvitationUpsertRequest {
    @JsonProperty("invitee_email")
    private String inviteeEmail;
    private String status;
    private String role;
    @JsonProperty("firm_name")
    private String firmName;
}

