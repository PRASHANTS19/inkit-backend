package com.inkit.backend.assignment.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaseAssignmentUpsertRequest {
    @JsonProperty("case_id")
    private UUID caseId;
    @JsonProperty("assigned_to_user_id")
    private UUID assignedToUserId;
    @JsonProperty("assigned_by_user_id")
    private UUID assignedByUserId;
    @JsonProperty("assignment_date")
    private LocalDateTime assignmentDate;
    @JsonProperty("role_in_case")
    private String roleInCase;
    private Map<String, Object> permissions;
    private String notes;
}

