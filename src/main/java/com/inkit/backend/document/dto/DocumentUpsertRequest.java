package com.inkit.backend.document.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentUpsertRequest {
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
    private List<String> tags;
}
