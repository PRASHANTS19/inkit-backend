package com.inkit.backend.document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Convert;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;
    private UUID caseId;
    private String documentType;
    @Column(length = 2000)
    private String description;
    private Boolean isConfidential;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
    private LocalDateTime uploadDate;
    private UUID uploadedBy;
    private UUID firmId;
    @Convert(converter = StringListConverter.class)
    @Column(length = 5000)
    private List<String> tags;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdDate = now;
        updatedDate = now;
        if (uploadDate == null) {
            uploadDate = now;
        }
        if (isConfidential == null) {
            isConfidential = false;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    @Converter
    public static class StringListConverter implements AttributeConverter<List<String>, String> {
        @Override
        public String convertToDatabaseColumn(List<String> attribute) {
            if (attribute == null || attribute.isEmpty()) return null;
            return String.join("||", attribute);
        }

        @Override
        public List<String> convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isBlank()) return List.of();
            return List.of(dbData.split("\\|\\|"));
        }
    }
}
