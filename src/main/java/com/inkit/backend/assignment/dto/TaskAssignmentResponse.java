package com.inkit.backend.assignment.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskAssignmentResponse {
    private UUID id;
    @JsonProperty("task_id")
    private UUID taskId;
    @JsonProperty("case_id")
    private UUID caseId;
    @JsonProperty("assigned_to_user_id")
    private UUID assignedToUserId;
    @JsonProperty("assignment_date")
    private LocalDateTime assignmentDate;
}

