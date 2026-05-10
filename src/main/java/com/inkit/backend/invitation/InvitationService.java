package com.inkit.backend.invitation;

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
import com.inkit.backend.common.enums.Role;
import com.inkit.backend.invitation.dto.InvitationFilterRequest;
import com.inkit.backend.invitation.dto.InvitationResponse;
import com.inkit.backend.invitation.dto.InvitationUpsertRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;

    public List<InvitationResponse> listInvitations(String userEmail, String status) {
        User user = getUserByEmail(userEmail);
        Specification<Invitation> spec = visibilityScope(user);
        spec = andIfPresent(spec, equalsIgnoreCase("status", status));

        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");
        return invitationRepository.findAll(spec, sort).stream().map(this::toResponse).toList();
    }

    public List<InvitationResponse> filterInvitations(String userEmail, InvitationFilterRequest request) {
        User user = getUserByEmail(userEmail);
        Specification<Invitation> spec = visibilityScope(user);
        spec = andIfPresent(spec, filterSpec(request.getFilter()));
        Pageable pageable = PageRequest.of(0, sanitizeLimit(request.getLimit()), parseSort(request.getSortBy(), "-created_date"));
        return invitationRepository.findAll(spec, pageable).getContent().stream().map(this::toResponse).toList();
    }

    public InvitationResponse getById(String userEmail, UUID id) {
        User user = getUserByEmail(userEmail);
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));
        ensureVisibleToUser(user, invitation);
        return toResponse(invitation);
    }

    public InvitationResponse create(String userEmail, InvitationUpsertRequest request) {
        User user = getUserByEmail(userEmail);
        ensureRole(user, Role.FIRM_ADMIN);

        Invitation invitation = Invitation.builder()
                .inviter(user)
                .inviteeEmail(request.getInviteeEmail())
                .status(request.getStatus() == null ? "pending" : request.getStatus())
                .role(request.getRole())
                .firmName(request.getFirmName() != null ? request.getFirmName() : (user.getFirm() != null ? user.getFirm().getName() : null))
                .build();
        return toResponse(invitationRepository.save(invitation));
    }

    public InvitationResponse update(String userEmail, UUID id, InvitationUpsertRequest request) {
        User user = getUserByEmail(userEmail);
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));
        ensureVisibleToUser(user, invitation);

        if (request.getInviteeEmail() != null) {
            invitation.setInviteeEmail(request.getInviteeEmail());
        }
        if (request.getStatus() != null) {
            invitation.setStatus(request.getStatus());
            if ("accepted".equalsIgnoreCase(request.getStatus())) {
                invitation.setAcceptedAt(LocalDateTime.now());
            }
        }
        if (request.getRole() != null) {
            invitation.setRole(request.getRole());
        }
        if (request.getFirmName() != null) {
            invitation.setFirmName(request.getFirmName());
        }

        return toResponse(invitationRepository.save(invitation));
    }

    public void accept(String userEmail, UUID id) {
        User user = getUserByEmail(userEmail);
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));

        if (!user.getEmail().equalsIgnoreCase(invitation.getInviteeEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized");
        }

        invitation.setStatus("accepted");
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        User inviter = invitation.getInviter();
        if (inviter != null) {
            user.setFirmAdmin(inviter);
            user.setFirm(inviter.getFirm());
        }
        user.setRole(fromAccountType(invitation.getRole() == null ? "associate" : invitation.getRole()));
        userRepository.save(user);
    }

    public void decline(String userEmail, UUID id) {
        User user = getUserByEmail(userEmail);
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));
        if (!user.getEmail().equalsIgnoreCase(invitation.getInviteeEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized");
        }
        invitation.setStatus("declined");
        invitationRepository.save(invitation);
    }

    public void delete(String userEmail, UUID id) {
        User user = getUserByEmail(userEmail);
        ensureRole(user, Role.FIRM_ADMIN);
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));
        if (invitation.getInviter() == null || !user.getId().equals(invitation.getInviter().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized");
        }
        invitationRepository.delete(invitation);
    }

    private InvitationResponse toResponse(Invitation i) {
        return InvitationResponse.builder()
                .id(i.getId())
                .inviterId(i.getInviter() != null ? i.getInviter().getId() : null)
                .inviterName(i.getInviter() != null ? i.getInviter().getName() : null)
                .inviteeEmail(i.getInviteeEmail())
                .status(i.getStatus())
                .role(i.getRole())
                .firmName(i.getFirmName())
                .acceptedAt(i.getAcceptedAt())
                .createdDate(i.getCreatedDate())
                .updatedDate(i.getUpdatedDate())
                .build();
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

    private void ensureVisibleToUser(User user, Invitation invitation) {
        if (user.getRole() == Role.FIRM_ADMIN) {
            if (invitation.getInviter() == null || !user.getId().equals(invitation.getInviter().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized");
            }
            return;
        }
        if (!user.getEmail().equalsIgnoreCase(invitation.getInviteeEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized");
        }
    }

    private Specification<Invitation> visibilityScope(User user) {
        if (user.getRole() == Role.FIRM_ADMIN) {
            return (root, query, cb) -> cb.equal(root.get("inviter").get("id"), user.getId());
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get("inviteeEmail")), user.getEmail().toLowerCase());
    }

    private Specification<Invitation> filterSpec(Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) {
            return null;
        }
        List<Specification<Invitation>> specs = new ArrayList<>();
        specs.add(uuidEquals("inviter", stringValue(filter.get("inviter_id"))));
        specs.add(equalsIgnoreCase("inviteeEmail", stringValue(filter.get("invitee_email"))));
        specs.add(equalsIgnoreCase("status", stringValue(filter.get("status"))));
        specs.add(equalsIgnoreCase("firmName", stringValue(filter.get("firm_name"))));

        Specification<Invitation> result = null;
        for (Specification<Invitation> s : specs) {
            if (s == null) {
                continue;
            }
            result = result == null ? Specification.where(s) : result.and(s);
        }
        return result;
    }

    private Specification<Invitation> uuidEquals(String field, String value) {
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

    private Specification<Invitation> equalsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get(field)), value.toLowerCase());
    }

    private Specification<Invitation> andIfPresent(Specification<Invitation> base, Specification<Invitation> other) {
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
            case "invitee_email" -> "inviteeEmail";
            case "firm_name" -> "firmName";
            default -> field;
        };
    }

    private Role fromAccountType(String accountType) {
        String normalized = accountType.trim().toLowerCase();
        return switch (normalized) {
            case "law_firm_admin" -> Role.FIRM_ADMIN;
            case "independent_advocate" -> Role.INDEPENDENT;
            case "associate" -> Role.ASSOCIATE;
            case "client" -> Role.CLIENT;
            case "intern" -> Role.INTERN;
            case "clerk" -> Role.CLERK;
            case "super_admin" -> Role.SUPER_ADMIN;
            default -> Role.ASSOCIATE;
        };
    }
}

