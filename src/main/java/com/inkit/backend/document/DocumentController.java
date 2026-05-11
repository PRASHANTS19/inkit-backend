package com.inkit.backend.document;

import java.util.List;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.inkit.backend.document.dto.DocumentFilterRequest;
import com.inkit.backend.document.dto.DocumentResponse;
import com.inkit.backend.document.dto.DocumentUpsertRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public List<DocumentResponse> listDocuments(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, name = "case_id") String caseId,
            @RequestParam(defaultValue = "-created_date") String sortBy,
            @RequestParam(defaultValue = "50") Integer limit) {
        return documentService.listDocuments(userDetails.getUsername(), caseId, sortBy, limit);
    }

    @PostMapping("/filter")
    public List<DocumentResponse> filterDocuments(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DocumentFilterRequest request) {
        return documentService.filterDocuments(userDetails.getUsername(), request);
    }

    @GetMapping("/{id}")
    public DocumentResponse getById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return documentService.getById(userDetails.getUsername(), id);
    }

    @PostMapping
    public DocumentResponse create(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DocumentUpsertRequest request) {
        return documentService.create(userDetails.getUsername(), request);
    }

    @PostMapping("/upload")
    public DocumentResponse upload(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false, name = "case_id") UUID caseId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false, name = "document_type") String documentType,
            @RequestParam(required = false) String description,
            @RequestParam(required = false, name = "is_confidential") Boolean isConfidential) throws Exception {
        String uploadDir = System.getenv().getOrDefault("UPLOAD_DIR", "./uploads");
        Files.createDirectories(Paths.get(uploadDir));
        String originalName = file.getOriginalFilename() == null ? "upload.bin" : file.getOriginalFilename();
        String storedName = System.currentTimeMillis() + "-" + UUID.randomUUID() + "-" + originalName;
        Path target = Paths.get(uploadDir).resolve(storedName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        DocumentUpsertRequest request = new DocumentUpsertRequest();
        request.setCaseId(caseId);
        request.setTitle(title != null && !title.isBlank() ? title : originalName);
        request.setDocumentType(documentType != null && !documentType.isBlank() ? documentType : "evidence");
        request.setDescription(description);
        request.setIsConfidential(Boolean.TRUE.equals(isConfidential));
        request.setFileUrl("/uploads/" + storedName);
        request.setFileSize(file.getSize());
        request.setFileType(contentExt(originalName));
        request.setUploadDate(LocalDateTime.now());
        return documentService.create(userDetails.getUsername(), request);
    }

    @PutMapping("/{id}")
    public DocumentResponse update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestBody DocumentUpsertRequest request) {
        return documentService.update(userDetails.getUsername(), id, request);
    }

    @DeleteMapping("/{id}")
    public String delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        documentService.delete(userDetails.getUsername(), id);
        return "Document deleted successfully";
    }

    private String contentExt(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) return "BIN";
        return filename.substring(idx + 1).toUpperCase();
    }
}
