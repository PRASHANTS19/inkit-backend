package com.inkit.backend.assignment.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskAssignmentUpsertRequest {
    @JsonProperty("task_id")
    private UUID taskId;
    @JsonProperty("user_id")
    private UUID userId;
}

