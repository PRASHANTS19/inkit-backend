package com.inkit.backend.hearing.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HearingResponse {
    private UUID id;
    @JsonProperty("hearing_date")
    private LocalDateTime hearingDate;
    private String status;
    private String courtroom;
    @JsonProperty("hearing_type")
    private String hearingType;
    private String notes;
    @JsonProperty("created_date")
    private LocalDateTime createdDate;
    @JsonProperty("updated_date")
    private LocalDateTime updatedDate;
    @JsonProperty("case_id")
    private UUID caseId;
    @JsonProperty("case_title")
    private String caseTitle;
    @JsonProperty("case_number")
    private String caseNumber;
    @JsonProperty("client_name")
    private String clientName;
}
