package com.inkit.backend.document.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentResponse {
    private UUID id;
    private String title;
    @JsonProperty("case_id")
    private UUID caseId;
    @JsonProperty("document_type")
    private String documentType;
    private String description;
    @JsonProperty("is_confidential")
    private Boolean isConfidential;
    @JsonProperty("file_url")
    private String fileUrl;
    @JsonProperty("file_size")
    private Long fileSize;
    @JsonProperty("file_type")
    private String fileType;
    @JsonProperty("upload_date")
    private LocalDateTime uploadDate;
    @JsonProperty("uploaded_by")
    private UUID uploadedBy;
    @JsonProperty("firm_id")
    private UUID firmId;
    private List<String> tags;
    @JsonProperty("created_date")
    private LocalDateTime createdDate;
    @JsonProperty("updated_date")
    private LocalDateTime updatedDate;
}
