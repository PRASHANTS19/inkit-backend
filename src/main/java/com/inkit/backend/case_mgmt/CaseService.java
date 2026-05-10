package com.inkit.backend.case_mgmt;

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
import com.inkit.backend.case_mgmt.dto.CaseFilterRequest;
import com.inkit.backend.common.enums.CaseType;
import com.inkit.backend.common.enums.Priority;
import com.inkit.backend.common.enums.Role;
import com.inkit.backend.common.enums.Status;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CaseService {

    private final CaseRepository caseRepository;
    private final UserRepository userRepository;

    public List<Case> listCases(String userEmail, String sortBy, Integer limit, String status,
            String caseType, String priority, String clientName, String search) {
        User user = getUserByEmail(userEmail);

        Specification<Case> spec = roleScopeSpec(user);
        spec = andIfPresent(spec, enumEquals("status", status, Status.class));
        spec = andIfPresent(spec, enumEquals("caseType", caseType, CaseType.class));
        spec = andIfPresent(spec, enumEquals("priority", priority, Priority.class));
        spec = andIfPresent(spec, containsIgnoreCase("clientName", clientName));
        spec = andIfPresent(spec, searchSpec(search));

        Pageable pageable = PageRequest.of(0, sanitizeLimit(limit), parseSort(sortBy));
        return caseRepository.findAll(spec, pageable).getContent();
    }

    public List<Case> filterCases(String userEmail, CaseFilterRequest request) {
        User user = getUserByEmail(userEmail);

        Specification<Case> spec = roleScopeSpec(user);
        spec = andIfPresent(spec, filterMapSpec(request.getFilter()));

        Pageable pageable = PageRequest.of(0, sanitizeLimit(request.getLimit()), parseSort(request.getSortBy()));
        return caseRepository.findAll(spec, pageable).getContent();
    }

    public Case getCaseById(String userEmail, UUID caseId) {
        User user = getUserByEmail(userEmail);
        Case caseData = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Case not found"));

        if (!isAccessible(user, caseData)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this case");
        }

        return caseData;
    }

    public Case createCase(String userEmail, Case request) {
        User user = getUserByEmail(userEmail);
        request.setId(null);
        request.setCreator(user);
        if (request.getAssignedTo() == null && user.getRole() == Role.INDEPENDENT) {
            request.setAssignedTo(user);
        }
        if (request.getFirm() == null) {
            request.setFirm(user.getFirm());
        }
        return caseRepository.save(request);
    }

    public Case updateCase(String userEmail, UUID caseId, Case request) {
        User user = getUserByEmail(userEmail);
        Case existing = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Case not found"));

        if (!isAccessible(user, existing)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot update this case");
        }

        existing.setCaseTitle(request.getCaseTitle());
        existing.setCaseNumber(request.getCaseNumber());
        existing.setClientName(request.getClientName());
        existing.setClientContact(request.getClientContact());
        existing.setCourt(request.getCourt());
        existing.setCaseType(request.getCaseType());
        existing.setStatus(request.getStatus());
        existing.setPriority(request.getPriority());
        existing.setNextHearingDate(request.getNextHearingDate());
        existing.setCaseDescription(request.getCaseDescription());
        existing.setOpposingCounsel(request.getOpposingCounsel());
        existing.setCaseValue(request.getCaseValue());
        existing.setFilingDate(request.getFilingDate());
        existing.setTags(request.getTags());
        existing.setAssignedTo(request.getAssignedTo());
        existing.setClient(request.getClient());
        existing.setFirm(request.getFirm() != null ? request.getFirm() : existing.getFirm());

        return caseRepository.save(existing);
    }

    public void deleteCase(String userEmail, UUID caseId) {
        User user = getUserByEmail(userEmail);
        Case existing = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Case not found"));

        if (!isAccessible(user, existing)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot delete this case");
        }

        caseRepository.delete(existing);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private boolean isAccessible(User user, Case caseData) {
        Role role = user.getRole();
        if (role == Role.ASSOCIATE) {
            return caseData.getAssignedTo() != null && user.getId().equals(caseData.getAssignedTo().getId());
        }
        if (role == Role.CLIENT) {
            return caseData.getClient() != null && caseData.getClient().getUsers().stream()
                    .anyMatch(u -> user.getId().equals(u.getId()));
        }

        boolean isCreator = caseData.getCreator() != null && user.getId().equals(caseData.getCreator().getId());
        boolean sameFirm = user.getFirm() != null && caseData.getFirm() != null
                && user.getFirm().getID().equals(caseData.getFirm().getID());
        return isCreator || sameFirm;
    }

    private Specification<Case> roleScopeSpec(User user) {
        Role role = user.getRole();
        if (role == Role.ASSOCIATE) {
            return (root, query, cb) -> cb.equal(root.get("assignedTo").get("id"), user.getId());
        }
        if (role == Role.CLIENT) {
            return (root, query, cb) -> cb.equal(root.join("client").join("users").get("id"), user.getId());
        }

        return (root, query, cb) -> cb.or(
                cb.equal(root.get("creator").get("id"), user.getId()),
                cb.equal(root.get("firm").get("id"), user.getFirm() != null ? user.getFirm().getID() : null));
    }

    private Specification<Case> searchSpec(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String like = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("caseTitle")), like),
                cb.like(cb.lower(root.get("caseNumber")), like),
                cb.like(cb.lower(root.get("clientName")), like));
    }

    private Specification<Case> containsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String like = "%" + value.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(field)), like);
    }

    private <T extends Enum<T>> Specification<Case> enumEquals(String field, String value, Class<T> enumType) {
        if (value == null || value.isBlank()) {
            return null;
        }

        T enumValue;
        try {
            enumValue = Enum.valueOf(enumType, value.trim().toUpperCase().replace('-', '_'));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + field + " value: " + value);
        }

        return (root, query, cb) -> cb.equal(root.get(field), enumValue);
    }

    private Specification<Case> filterMapSpec(Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) {
            return null;
        }

        List<Specification<Case>> specs = new ArrayList<>();
        specs.add(enumEquals("status", stringValue(filter.get("status")), Status.class));
        specs.add(enumEquals("caseType", stringValue(filter.get("caseType")), CaseType.class));
        specs.add(enumEquals("priority", stringValue(filter.get("priority")), Priority.class));
        specs.add(containsIgnoreCase("clientName", stringValue(filter.get("clientName"))));

        String title = stringValue(filter.get("caseTitle"));
        if (title != null && !title.isBlank()) {
            specs.add(containsIgnoreCase("caseTitle", title));
        }

        String number = stringValue(filter.get("caseNumber"));
        if (number != null && !number.isBlank()) {
            specs.add(containsIgnoreCase("caseNumber", number));
        }

        Specification<Case> result = null;
        for (Specification<Case> spec : specs) {
            if (spec == null) {
                continue;
            }
            result = result == null ? Specification.where(spec) : result.and(spec);
        }
        return result;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Specification<Case> andIfPresent(Specification<Case> base, Specification<Case> other) {
        return other == null ? base : base.and(other);
    }

    private int sanitizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 50;
        }
        return Math.min(limit, 200);
    }

    private Sort parseSort(String sortBy) {
        String safeSort = (sortBy == null || sortBy.isBlank()) ? "-createdDate" : sortBy;
        Sort.Direction direction = safeSort.startsWith("-") ? Sort.Direction.DESC : Sort.Direction.ASC;
        String field = safeSort.startsWith("-") ? safeSort.substring(1) : safeSort;
        return Sort.by(direction, mapSortField(field));
    }

    private String mapSortField(String field) {
        return switch (field) {
            case "created_date" -> "createdDate";
            case "updated_date" -> "updatedDate";
            case "case_title" -> "caseTitle";
            case "case_number" -> "caseNumber";
            case "client_name" -> "clientName";
            case "next_hearing_date" -> "nextHearingDate";
            default -> field;
        };
    }
}
