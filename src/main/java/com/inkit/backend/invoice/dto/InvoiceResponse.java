package com.inkit.backend.invoice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InvoiceResponse {
    private UUID id;
    @JsonProperty("invoice_number")
    private String invoiceNumber;
    private String status;
    private Double amount;
    private String notes;
    @JsonProperty("issue_date")
    private LocalDate issueDate;
    @JsonProperty("due_date")
    private LocalDate dueDate;
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
    @JsonProperty("created_by")
    private UUID createdBy;
}

