package com.inkit.backend.hearing.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HearingUpsertRequest {
    @JsonProperty("hearing_date")
    private LocalDateTime hearingDate;
    private String status;
    private String courtroom;
    @JsonProperty("hearing_type")
    private String hearingType;
    private String notes;
    @JsonProperty("case_id")
    private UUID caseId;
}

