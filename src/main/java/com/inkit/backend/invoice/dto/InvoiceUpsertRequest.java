package com.inkit.backend.invoice.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceUpsertRequest {
    @JsonProperty("invoice_number")
    private String invoiceNumber;
    private String status;
    private Double amount;
    private String notes;
    @JsonProperty("issue_date")
    private LocalDate issueDate;
    @JsonProperty("due_date")
    private LocalDate dueDate;
    @JsonProperty("case_id")
    private UUID caseId;
}

