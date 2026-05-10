package com.inkit.backend.invitation.dto;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvitationFilterRequest {
    private Map<String, Object> filter;
    private String sortBy;
    private Integer limit;
}

