package com.inkit.backend.hearing;

import java.time.LocalDateTime;
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
import com.inkit.backend.case_mgmt.Case;
import com.inkit.backend.case_mgmt.CaseRepository;
import com.inkit.backend.hearing.dto.HearingFilterRequest;
import com.inkit.backend.hearing.dto.HearingResponse;
import com.inkit.backend.hearing.dto.HearingUpsertRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HearingService {

    private final HearingRepository hearingRepository;
    private final UserRepository userRepository;
    private final CaseRepository caseRepository;

    public List<HearingResponse> listHearings(String userEmail, String caseId, String status,
            String sortBy, Integer limit, Boolean upcoming) {
        User user = getUserByEmail(userEmail);
        Specification<Hearing> spec = sameFirmSpec(user);
        spec = andIfPresent(spec, uuidEquals("caseRef", caseId));
        spec = andIfPresent(spec, equalsIgnoreCase("status", status));

        if (Boolean.TRUE.equals(upcoming)) {
            spec = andIfPresent(spec, hearingDateGte(LocalDateTime.now()));
            spec = andIfPresent(spec, equalsIgnoreCase("status", "scheduled"));
        }

        Pageable pageable = PageRequest.of(0, sanitizeLimit(limit), parseSort(sortBy, "hearing_date"));
        return hearingRepository.findAll(spec, pageable).getContent().stream().map(this::toResponse).toList();
    }

    public List<HearingResponse> filterHearings(String userEmail, HearingFilterRequest request) {
        User user = getUserByEmail(userEmail);
        Specification<Hearing> spec = sameFirmSpec(user);
        spec = andIfPresent(spec, filterSpec(request.getFilter()));
        Pageable pageable = PageRequest.of(0, sanitizeLimit(request.getLimit()), parseSort(request.getSortBy(), "hearing_date"));
        return hearingRepository.findAll(spec, pageable).getContent().stream().map(this::toResponse).toList();
    }

    public HearingResponse getById(String userEmail, UUID id) {
        User user = getUserByEmail(userEmail);
        Hearing hearing = hearingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hearing not found"));
        ensureSameFirm(user, hearing);
        return toResponse(hearing);
    }

    public HearingResponse create(String userEmail, HearingUpsertRequest request) {
        User user = getUserByEmail(userEmail);
        Hearing hearing = Hearing.builder()
                .hearingDate(request.getHearingDate())
                .status(request.getStatus())
                .courtroom(request.getCourtroom())
                .hearingType(request.getHearingType())
                .notes(request.getNotes())
                .firm(user.getFirm())
                .build();
        if (request.getCaseId() != null) {
            hearing.setCaseRef(findCase(request.getCaseId()));
        }
        return toResponse(hearingRepository.save(hearing));
    }

    public HearingResponse update(String userEmail, UUID id, HearingUpsertRequest request) {
        User user = getUserByEmail(userEmail);
        Hearing existing = hearingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hearing not found"));
        ensureSameFirm(user, existing);

        if (request.getHearingDate() != null) {
            existing.setHearingDate(request.getHearingDate());
        }
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        if (request.getCourtroom() != null) {
            existing.setCourtroom(request.getCourtroom());
        }
        if (request.getHearingType() != null) {
            existing.setHearingType(request.getHearingType());
        }
        if (request.getNotes() != null) {
            existing.setNotes(request.getNotes());
        }
        if (request.getCaseId() != null) {
            existing.setCaseRef(findCase(request.getCaseId()));
        }
        return toResponse(hearingRepository.save(existing));
    }

    public void delete(String userEmail, UUID id) {
        User user = getUserByEmail(userEmail);
        Hearing existing = hearingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hearing not found"));
        ensureSameFirm(user, existing);
        hearingRepository.delete(existing);
    }

    private HearingResponse toResponse(Hearing h) {
        Case c = h.getCaseRef();
        return HearingResponse.builder()
                .id(h.getId())
                .hearingDate(h.getHearingDate())
                .status(h.getStatus())
                .courtroom(h.getCourtroom())
                .hearingType(h.getHearingType())
                .notes(h.getNotes())
                .createdDate(h.getCreatedDate())
                .updatedDate(h.getUpdatedDate())
                .caseId(c != null ? c.getId() : null)
                .caseTitle(c != null ? c.getCaseTitle() : null)
                .caseNumber(c != null ? c.getCaseNumber() : null)
                .clientName(c != null ? c.getClientName() : null)
                .build();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private void ensureSameFirm(User user, Hearing hearing) {
        UUID userFirmId = user.getFirm() != null ? user.getFirm().getID() : null;
        UUID hearingFirmId = hearing.getFirm() != null ? hearing.getFirm().getID() : null;
        if (userFirmId == null || hearingFirmId == null || !userFirmId.equals(hearingFirmId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized");
        }
    }

    private Specification<Hearing> sameFirmSpec(User user) {
        UUID firmId = user.getFirm() != null ? user.getFirm().getID() : null;
        return (root, query, cb) -> cb.equal(root.get("firm").get("id"), firmId);
    }

    private Specification<Hearing> filterSpec(Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) {
            return null;
        }
        List<Specification<Hearing>> specs = new ArrayList<>();
        specs.add(uuidEquals("caseRef", stringValue(filter.get("case_id"))));
        specs.add(uuidEquals("caseRef", stringValue(filter.get("caseRef"))));
        specs.add(equalsIgnoreCase("status", stringValue(filter.get("status"))));
        specs.add(equalsIgnoreCase("hearingType", stringValue(filter.get("hearing_type"))));
        specs.add(equalsIgnoreCase("hearingType", stringValue(filter.get("hearingType"))));
        Specification<Hearing> result = null;
        for (Specification<Hearing> s : specs) {
            if (s == null) {
                continue;
            }
            result = result == null ? Specification.where(s) : result.and(s);
        }
        return result;
    }

    private Specification<Hearing> uuidEquals(String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        UUID id;
        try {
            id = UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid UUID: " + value);
        }
        return (root, query, cb) -> cb.equal(root.get(field).get("id"), id);
    }

    private Specification<Hearing> equalsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get(field)), value.toLowerCase());
    }

    private Case findCase(UUID id) {
        return caseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Case not found: " + id));
    }

    private Specification<Hearing> hearingDateGte(LocalDateTime date) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("hearingDate"), date);
    }

    private Specification<Hearing> andIfPresent(Specification<Hearing> base, Specification<Hearing> other) {
        return other == null ? base : base.and(other);
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private int sanitizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 50;
        }
        return Math.min(limit, 200);
    }

    private Sort parseSort(String sortBy, String fallback) {
        String safeSort = (sortBy == null || sortBy.isBlank()) ? fallback : sortBy;
        Sort.Direction direction = safeSort.startsWith("-") ? Sort.Direction.DESC : Sort.Direction.ASC;
        String field = safeSort.startsWith("-") ? safeSort.substring(1) : safeSort;
        return Sort.by(direction, mapSortField(field));
    }

    private String mapSortField(String field) {
        return switch (field) {
            case "hearing_date" -> "hearingDate";
            case "created_date" -> "createdDate";
            case "updated_date" -> "updatedDate";
            default -> field;
        };
    }
}
