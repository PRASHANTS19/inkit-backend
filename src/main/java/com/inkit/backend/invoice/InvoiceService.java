package com.inkit.backend.invoice;

import java.util.ArrayList;
import java.util.Collection;
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
import com.inkit.backend.common.enums.Role;
import com.inkit.backend.invoice.dto.InvoiceFilterRequest;
import com.inkit.backend.invoice.dto.InvoiceResponse;
import com.inkit.backend.invoice.dto.InvoiceUpsertRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final CaseRepository caseRepository;

    public List<InvoiceResponse> listInvoices(String userEmail, String caseId, String status, String sortBy, Integer limit) {
        User user = getUserByEmail(userEmail);
        ensureRole(user, Role.FIRM_ADMIN, Role.INDEPENDENT);

        Specification<Invoice> spec = createdByUser(user);
        spec = andIfPresent(spec, uuidEquals("caseRef", caseId));
        spec = andIfPresent(spec, equalsIgnoreCase("status", status));

        Pageable pageable = PageRequest.of(0, sanitizeLimit(limit), parseSort(sortBy, "-created_date"));
        return invoiceRepository.findAll(spec, pageable).getContent().stream().map(this::toResponse).toList();
    }

    public List<InvoiceResponse> filterInvoices(String userEmail, InvoiceFilterRequest request) {
        User user = getUserByEmail(userEmail);
        ensureRole(user, Role.FIRM_ADMIN, Role.INDEPENDENT);

        Specification<Invoice> spec = createdByUser(user);
        spec = andIfPresent(spec, filterSpec(request.getFilter()));
        Pageable pageable = PageRequest.of(0, sanitizeLimit(request.getLimit()), parseSort(request.getSortBy(), "-created_date"));
        return invoiceRepository.findAll(spec, pageable).getContent().stream().map(this::toResponse).toList();
    }

    public InvoiceResponse getById(String userEmail, UUID id) {
        User user = getUserByEmail(userEmail);
        ensureRole(user, Role.FIRM_ADMIN, Role.INDEPENDENT);
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        if (invoice.getCreatedBy() == null || !user.getId().equals(invoice.getCreatedBy().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized");
        }
        return toResponse(invoice);
    }

    public InvoiceResponse create(String userEmail, InvoiceUpsertRequest request) {
        User user = getUserByEmail(userEmail);
        ensureRole(user, Role.FIRM_ADMIN, Role.INDEPENDENT);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(request.getInvoiceNumber())
                .status(request.getStatus())
                .amount(request.getAmount())
                .notes(request.getNotes())
                .issueDate(request.getIssueDate())
                .dueDate(request.getDueDate())
                .createdBy(user)
                .firm(user.getFirm())
                .build();
        if (request.getCaseId() != null) {
            invoice.setCaseRef(findCase(request.getCaseId()));
        }
        return toResponse(invoiceRepository.save(invoice));
    }

    public InvoiceResponse update(String userEmail, UUID id, InvoiceUpsertRequest request) {
        User user = getUserByEmail(userEmail);
        ensureRole(user, Role.FIRM_ADMIN, Role.INDEPENDENT);
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        if (invoice.getCreatedBy() == null || !user.getId().equals(invoice.getCreatedBy().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized");
        }

        if (request.getInvoiceNumber() != null) {
            invoice.setInvoiceNumber(request.getInvoiceNumber());
        }
        if (request.getStatus() != null) {
            invoice.setStatus(request.getStatus());
        }
        if (request.getAmount() != null) {
            invoice.setAmount(request.getAmount());
        }
        if (request.getNotes() != null) {
            invoice.setNotes(request.getNotes());
        }
        if (request.getIssueDate() != null) {
            invoice.setIssueDate(request.getIssueDate());
        }
        if (request.getDueDate() != null) {
            invoice.setDueDate(request.getDueDate());
        }
        if (request.getCaseId() != null) {
            invoice.setCaseRef(findCase(request.getCaseId()));
        }
        return toResponse(invoiceRepository.save(invoice));
    }

    public void delete(String userEmail, UUID id) {
        User user = getUserByEmail(userEmail);
        ensureRole(user, Role.FIRM_ADMIN, Role.INDEPENDENT);
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        if (invoice.getCreatedBy() == null || !user.getId().equals(invoice.getCreatedBy().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized");
        }
        invoiceRepository.delete(invoice);
    }

    private InvoiceResponse toResponse(Invoice inv) {
        Case c = inv.getCaseRef();
        return InvoiceResponse.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .status(inv.getStatus())
                .amount(inv.getAmount())
                .notes(inv.getNotes())
                .issueDate(inv.getIssueDate())
                .dueDate(inv.getDueDate())
                .createdDate(inv.getCreatedDate())
                .updatedDate(inv.getUpdatedDate())
                .caseId(c != null ? c.getId() : null)
                .caseTitle(c != null ? c.getCaseTitle() : null)
                .caseNumber(c != null ? c.getCaseNumber() : null)
                .clientName(c != null ? c.getClientName() : null)
                .createdBy(inv.getCreatedBy() != null ? inv.getCreatedBy().getId() : null)
                .build();
    }

    private Case findCase(UUID id) {
        return caseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Case not found: " + id));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private void ensureRole(User user, Role... allowed) {
        for (Role role : allowed) {
            if (user.getRole() == role) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized");
    }

    private Specification<Invoice> createdByUser(User user) {
        return (root, query, cb) -> cb.equal(root.get("createdBy").get("id"), user.getId());
    }

    private Specification<Invoice> filterSpec(Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) {
            return null;
        }
        List<Specification<Invoice>> specs = new ArrayList<>();
        specs.add(uuidEquals("caseRef", stringValue(filter.get("case_id"))));
        specs.add(equalsIgnoreCase("status", stringValue(filter.get("status"))));
        specs.add(inSpec("status", filter.get("status")));

        Specification<Invoice> result = null;
        for (Specification<Invoice> s : specs) {
            if (s == null) {
                continue;
            }
            result = result == null ? Specification.where(s) : result.and(s);
        }
        return result;
    }

    private Specification<Invoice> uuidEquals(String field, String value) {
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

    @SuppressWarnings("unchecked")
    private Specification<Invoice> inSpec(String field, Object raw) {
        if (!(raw instanceof Map<?, ?> map)) {
            return null;
        }
        Object inValue = map.get("$in");
        if (!(inValue instanceof Collection<?> values) || values.isEmpty()) {
            return null;
        }
        List<String> normalized = values.stream().map(String::valueOf).map(String::toLowerCase).toList();
        return (root, query, cb) -> cb.lower(root.get(field)).in(normalized);
    }

    private Specification<Invoice> equalsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get(field)), value.toLowerCase());
    }

    private Specification<Invoice> andIfPresent(Specification<Invoice> base, Specification<Invoice> other) {
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
            case "created_date" -> "createdDate";
            case "updated_date" -> "updatedDate";
            case "issue_date" -> "issueDate";
            case "due_date" -> "dueDate";
            default -> field;
        };
    }
}

