package com.inkit.backend.task.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskResponse {
    private UUID id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private LocalDate dueDate;
    private String taskType;
    private Double estimatedHours;
    private Double actualHours;
    private String notes;
    private Boolean reminderSent;
    private LocalDateTime createdDate;

    private UUID caseId;
    private String caseTitle;
    private String caseNumber;

    private UUID assigneeId;
    private String assigneeName;
    private String assigneeEmail;
}
