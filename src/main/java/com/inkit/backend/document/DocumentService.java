package com.inkit.backend.document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.inkit.backend.auth.User;
import com.inkit.backend.auth.UserRepository;
import com.inkit.backend.common.enums.Role;
import com.inkit.backend.document.dto.DocumentFilterRequest;
import com.inkit.backend.document.dto.DocumentResponse;
import com.inkit.backend.document.dto.DocumentUpsertRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public List<DocumentResponse> listDocuments(String userEmail, String caseId, String sortBy, Integer limit) {
        User user = getUserByEmail(userEmail);
        Specification<Document> spec = byFirm(user.getFirm() != null ? user.getFirm().getID() : null);
        spec = andIfPresent(spec, uuidEquals("caseId", caseId));
        if (user.getRole() == Role.CLIENT) {
            spec = andIfPresent(spec, equalsBoolean("isConfidential", false));
        }
        Pageable pageable = PageRequest.of(0, sanitizeLimit(limit), parseSort(sortBy));
        return documentRepository.findAll(spec, pageable).getContent().stream().map(this::toResponse).toList();
    }

    public List<DocumentResponse> filterDocuments(String userEmail, DocumentFilterRequest request) {
        User user = getUserByEmail(userEmail);
        Specification<Document> spec = byFirm(user.getFirm() != null ? user.getFirm().getID() : null);
        spec = andIfPresent(spec, filterSpec(request.getFilter()));
        if (user.getRole() == Role.CLIENT) {
            spec = andIfPresent(spec, equalsBoolean("isConfidential", false));
        }
        Pageable pageable = PageRequest.of(0, sanitizeLimit(request.getLimit()), parseSort(request.getSortBy()));
        return documentRepository.findAll(spec, pageable).getContent().stream().map(this::toResponse).toList();
    }

    public DocumentResponse getById(String userEmail, UUID id) {
        User user = getUserByEmail(userEmail);
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        ensureFirmAccess(user, doc);
        return toResponse(doc);
    }

    public DocumentResponse create(String userEmail, DocumentUpsertRequest request) {
        User user = getUserByEmail(userEmail);
        Document doc = Document.builder()
                .title(request.getTitle())
                .caseId(request.getCaseId())
                .documentType(request.getDocumentType())
                .description(request.getDescription())
                .isConfidential(request.getIsConfidential())
                .fileUrl(request.getFileUrl())
                .fileSize(request.getFileSize())
                .fileType(request.getFileType())
                .uploadDate(request.getUploadDate())
                .uploadedBy(user.getId())
                .firmId(user.getFirm() != null ? user.getFirm().getID() : null)
                .tags(request.getTags())
                .build();
        return toResponse(documentRepository.save(doc));
    }

    public DocumentResponse update(String userEmail, UUID id, DocumentUpsertRequest request) {
        User user = getUserByEmail(userEmail);
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        ensureFirmAccess(user, doc);

        if (request.getTitle() != null) doc.setTitle(request.getTitle());
        if (request.getCaseId() != null) doc.setCaseId(request.getCaseId());
        if (request.getDocumentType() != null) doc.setDocumentType(request.getDocumentType());
        if (request.getDescription() != null) doc.setDescription(request.getDescription());
        if (request.getIsConfidential() != null) doc.setIsConfidential(request.getIsConfidential());
        if (request.getFileUrl() != null) doc.setFileUrl(request.getFileUrl());
        if (request.getFileSize() != null) doc.setFileSize(request.getFileSize());
        if (request.getFileType() != null) doc.setFileType(request.getFileType());
        if (request.getUploadDate() != null) doc.setUploadDate(request.getUploadDate());
        if (request.getTags() != null) doc.setTags(request.getTags());

        return toResponse(documentRepository.save(doc));
    }

    public void delete(String userEmail, UUID id) {
        User user = getUserByEmail(userEmail);
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        ensureFirmAccess(user, doc);
        documentRepository.delete(doc);
    }

    private DocumentResponse toResponse(Document doc) {
        return DocumentResponse.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .caseId(doc.getCaseId())
                .documentType(doc.getDocumentType())
                .description(doc.getDescription())
                .isConfidential(doc.getIsConfidential())
                .fileUrl(doc.getFileUrl())
                .fileSize(doc.getFileSize())
                .fileType(doc.getFileType())
                .uploadDate(doc.getUploadDate())
                .uploadedBy(doc.getUploadedBy())
                .firmId(doc.getFirmId())
                .tags(doc.getTags())
                .createdDate(doc.getCreatedDate())
                .updatedDate(doc.getUpdatedDate())
                .build();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private void ensureFirmAccess(User user, Document doc) {
        if (user.getFirm() == null || doc.getFirmId() == null || !user.getFirm().getID().equals(doc.getFirmId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized for this firm");
        }
    }

    private Specification<Document> byFirm(UUID firmId) {
        return (root, query, cb) -> cb.equal(root.get("firmId"), firmId);
    }

    private Specification<Document> filterSpec(Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) {
            return null;
        }
        List<Specification<Document>> specs = new ArrayList<>();
        specs.add(uuidEquals("caseId", stringValue(filter.get("case_id"))));
        specs.add(equalsIgnoreCase("documentType", stringValue(filter.get("document_type"))));

        Specification<Document> result = null;
        for (Specification<Document> spec : specs) {
            if (spec == null) continue;
            result = result == null ? Specification.where(spec) : result.and(spec);
        }
        return result;
    }

    private Specification<Document> uuidEquals(String field, String value) {
        if (value == null || value.isBlank()) return null;
        UUID id;
        try {
            id = UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid UUID: " + value);
        }
        return (root, query, cb) -> cb.equal(root.get(field), id);
    }

    private Specification<Document> equalsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) return null;
        return (root, query, cb) -> cb.equal(cb.lower(root.get(field)), value.toLowerCase());
    }

    private Specification<Document> equalsBoolean(String field, Boolean value) {
        if (value == null) return null;
        return (root, query, cb) -> cb.equal(root.get(field), value);
    }

    private Specification<Document> andIfPresent(Specification<Document> base, Specification<Document> other) {
        return other == null ? base : base.and(other);
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private int sanitizeLimit(Integer limit) {
        if (limit == null || limit < 1) return 50;
        return Math.min(limit, 200);
    }

    private Sort parseSort(String sortBy) {
        String safeSort = (sortBy == null || sortBy.isBlank()) ? "-created_date" : sortBy;
        Sort.Direction direction = safeSort.startsWith("-") ? Sort.Direction.DESC : Sort.Direction.ASC;
        String field = safeSort.startsWith("-") ? safeSort.substring(1) : safeSort;
        return Sort.by(direction, mapSortField(field));
    }

    private String mapSortField(String field) {
        return switch (field) {
            case "created_date" -> "createdDate";
            case "updated_date" -> "updatedDate";
            case "upload_date" -> "uploadDate";
            default -> field;
        };
    }
}
