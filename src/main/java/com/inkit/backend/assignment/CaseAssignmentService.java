package com.inkit.backend.assignment;

import java.util.ArrayList;
import java.util.Collections;
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

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.inkit.backend.assignment.dto.AssignmentFilterRequest;
import com.inkit.backend.assignment.dto.CaseAssignmentResponse;
import com.inkit.backend.assignment.dto.CaseAssignmentUpsertRequest;
import com.inkit.backend.auth.User;
import com.inkit.backend.auth.UserRepository;
import com.inkit.backend.common.enums.Role;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CaseAssignmentService {

    private final CaseAssignmentRepository repository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public List<CaseAssignmentResponse> list(String userEmail, String caseId, String userId, String sortBy, Integer limit) {
        User user = getUserByEmail(userEmail);
        Specification<CaseAssignment> spec = byFirm(user.getFirm() != null ? user.getFirm().getID() : null);
        spec = andIfPresent(spec, uuidEquals("caseId", caseId));
        spec = andIfPresent(spec, uuidEquals("assignedToUserId", userId));
        Pageable pageable = PageRequest.of(0, sanitizeLimit(limit), parseSort(sortBy));
        return repository.findAll(spec, pageable).getContent().stream().map(this::toResponse).toList();
    }

    public List<CaseAssignmentResponse> filter(String userEmail, AssignmentFilterRequest request) {
        User user = getUserByEmail(userEmail);
        Specification<CaseAssignment> spec = byFirm(user.getFirm() != null ? user.getFirm().getID() : null);
        spec = andIfPresent(spec, filterSpec(request.getFilter()));
        Pageable pageable = PageRequest.of(0, sanitizeLimit(request.getLimit()), parseSort(request.getSortBy()));
        return repository.findAll(spec, pageable).getContent().stream().map(this::toResponse).toList();
    }

    public CaseAssignmentResponse create(String userEmail, CaseAssignmentUpsertRequest request) {
        User user = getUserByEmail(userEmail);
        ensureRole(user, Role.FIRM_ADMIN);
        UUID assignerId = request.getAssignedByUserId() != null ? request.getAssignedByUserId() : user.getId();
        if (!assignerId.equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot assign on behalf of another user");
        }

        CaseAssignment assignment = CaseAssignment.builder()
                .caseId(request.getCaseId())
                .assignedToUserId(request.getAssignedToUserId())
                .assignedByUserId(assignerId)
                .assignmentDate(request.getAssignmentDate())
                .roleInCase(request.getRoleInCase())
                .permissionsJson(writeJson(request.getPermissions()))
                .notes(request.getNotes())
                .firmId(user.getFirm() != null ? user.getFirm().getID() : null)
                .build();
        return toResponse(repository.save(assignment));
    }

    public void delete(String userEmail, UUID id) {
        User user = getUserByEmail(userEmail);
        ensureRole(user, Role.FIRM_ADMIN);
        CaseAssignment assignment = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
        if (assignment.getFirmId() == null || user.getFirm() == null || !assignment.getFirmId().equals(user.getFirm().getID())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized");
        }
        repository.delete(assignment);
    }

    private CaseAssignmentResponse toResponse(CaseAssignment assignment) {
        return CaseAssignmentResponse.builder()
                .id(assignment.getId())
                .caseId(assignment.getCaseId())
                .assignedToUserId(assignment.getAssignedToUserId())
                .assignedByUserId(assignment.getAssignedByUserId())
                .assignmentDate(assignment.getAssignmentDate())
                .roleInCase(assignment.getRoleInCase())
                .permissions(readJsonMap(assignment.getPermissionsJson()))
                .notes(assignment.getNotes())
                .createdDate(assignment.getCreatedDate())
                .updatedDate(assignment.getUpdatedDate())
                .build();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private Specification<CaseAssignment> byFirm(UUID firmId) {
        return (root, query, cb) -> cb.equal(root.get("firmId"), firmId);
    }

    private Specification<CaseAssignment> filterSpec(Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) return null;
        List<Specification<CaseAssignment>> specs = new ArrayList<>();
        specs.add(uuidEquals("caseId", stringValue(filter.get("case_id"))));
        specs.add(uuidEquals("assignedToUserId", stringValue(filter.get("assigned_to_user_id"))));
        specs.add(uuidEquals("assignedByUserId", stringValue(filter.get("assigned_by_user_id"))));

        Specification<CaseAssignment> result = null;
        for (Specification<CaseAssignment> spec : specs) {
            if (spec == null) continue;
            result = result == null ? Specification.where(spec) : result.and(spec);
        }
        return result;
    }

    private Specification<CaseAssignment> uuidEquals(String field, String value) {
        if (value == null || value.isBlank()) return null;
        UUID id;
        try {
            id = UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid UUID: " + value);
        }
        return (root, query, cb) -> cb.equal(root.get(field), id);
    }

    private Specification<CaseAssignment> andIfPresent(Specification<CaseAssignment> base, Specification<CaseAssignment> other) {
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
        String safeSort = (sortBy == null || sortBy.isBlank()) ? "-assignment_date" : sortBy;
        Sort.Direction direction = safeSort.startsWith("-") ? Sort.Direction.DESC : Sort.Direction.ASC;
        String field = safeSort.startsWith("-") ? safeSort.substring(1) : safeSort;
        return Sort.by(direction, mapSortField(field));
    }

    private String mapSortField(String field) {
        return switch (field) {
            case "assignment_date" -> "assignmentDate";
            case "created_date" -> "createdDate";
            case "updated_date" -> "updatedDate";
            default -> field;
        };
    }

    private void ensureRole(User user, Role... allowed) {
        for (Role role : allowed) {
            if (user.getRole() == role) return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permission");
    }

    private String writeJson(Map<String, Object> data) {
        if (data == null || data.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid permissions payload");
        }
    }

    private Map<String, Object> readJsonMap(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }
}
