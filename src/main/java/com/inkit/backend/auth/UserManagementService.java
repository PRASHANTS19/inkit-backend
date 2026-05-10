package com.inkit.backend.auth;

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

import com.inkit.backend.auth.dto.UpdateManagedUserRequest;
import com.inkit.backend.auth.dto.UserFilterRequest;
import com.inkit.backend.auth.dto.UserSummaryResponse;
import com.inkit.backend.common.enums.Role;

import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;

    public List<UserSummaryResponse> listUsers(String requesterEmail, String accountType, String firmAdminId) {
        User requester = getUserByEmail(requesterEmail);
        ensureRole(requester, Role.FIRM_ADMIN, Role.INDEPENDENT);

        Specification<User> spec = emptySpec();

        if (accountType != null && !accountType.isBlank()) {
            spec = spec.and(accountTypeSpec(accountType));
        }

        if (firmAdminId != null && !firmAdminId.isBlank()) {
            spec = spec.and(firmAdminSpec(firmAdminId));
        }

        if (requester.getRole() == Role.FIRM_ADMIN) {
            spec = spec.and((root, query, cb) -> cb.equal(root.join("firmAdmin", JoinType.LEFT).get("id"), requester.getId()));
        }

        return userRepository.findAll(spec)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    private Specification<User> emptySpec() {
        return (root, query, cb) -> cb.conjunction();
    }

    public List<UserSummaryResponse> filterUsers(String requesterEmail, UserFilterRequest request) {
        getUserByEmail(requesterEmail);

        Specification<User> spec = filterMapSpec(request.getFilter());
        Pageable pageable = PageRequest.of(0, sanitizeLimit(request.getLimit()), parseSort(request.getSortBy()));

        return userRepository.findAll(spec, pageable).getContent().stream().map(this::toSummary).toList();
    }

    public UserSummaryResponse getUserById(String requesterEmail, UUID userId) {
        getUserByEmail(requesterEmail);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toSummary(user);
    }

    public UserSummaryResponse updateUser(String requesterEmail, UUID userId, UpdateManagedUserRequest request) {
        User requester = getUserByEmail(requesterEmail);
        ensureRole(requester, Role.FIRM_ADMIN);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.getName() != null)
            user.setName(request.getName());
        if (request.getBarRegistrationNumber() != null)
            user.setBarRegistrationNumber(request.getBarRegistrationNumber());
        if (request.getPhone() != null)
            user.setPhone(request.getPhone());
        if (request.getYOE() != null)
            user.setYOE(request.getYOE());
        if (request.getAreaOfSpecialization() != null)
            user.setAreaOfSpecialization(request.getAreaOfSpecialization());
        if (request.getStreetAddress() != null)
            user.setStreetAddress(request.getStreetAddress());
        if (request.getCity() != null)
            user.setCity(request.getCity());
        if (request.getState() != null)
            user.setState(request.getState());
        if (request.getPinCode() != null)
            user.setPinCode(request.getPinCode());
        if (request.getIsActive() != null)
            user.setActive(request.getIsActive());

        return toSummary(userRepository.save(user));
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
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permission");
    }

    private Specification<User> accountTypeSpec(String accountType) {
        if (accountType == null || accountType.isBlank()) {
            return null;
        }
        Role role = fromAccountType(accountType);
        return (root, query, cb) -> cb.equal(root.get("role"), role);
    }

    private Specification<User> firmAdminSpec(String firmAdminId) {
        if (firmAdminId == null || firmAdminId.isBlank()) {
            return null;
        }
        UUID id;
        try {
            id = UUID.fromString(firmAdminId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid firm_admin_id: " + firmAdminId);
        }
        return (root, query, cb) -> cb.equal(root.join("firmAdmin").get("id"), id);
    }

    private Specification<User> filterMapSpec(Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) {
            return null;
        }

        List<Specification<User>> specs = new ArrayList<>();
        specs.add(accountTypeSpec(stringValue(filter.get("account_type"))));
        specs.add(firmAdminSpec(stringValue(filter.get("firm_admin_id"))));
        specs.add(boolEquals("isActive", filter.get("is_active")));
        specs.add(containsIgnoreCase("name", stringValue(filter.get("full_name"))));
        specs.add(containsIgnoreCase("email", stringValue(filter.get("email"))));

        Specification<User> result = null;
        for (Specification<User> spec : specs) {
            if (spec == null)
                continue;
            result = result == null ? spec : result.and(spec);
        }
        return result;
    }

    private Specification<User> boolEquals(String field, Object value) {
        if (value == null) {
            return null;
        }
        boolean parsed = Boolean.parseBoolean(String.valueOf(value));
        return (root, query, cb) -> cb.equal(root.get(field), parsed);
    }

    private Specification<User> containsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank())
            return null;
        String like = "%" + value.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(field)), like);
    }

    private int sanitizeLimit(Integer limit) {
        if (limit == null || limit < 1)
            return 50;
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
            case "full_name" -> "name";
            case "account_type" -> "role";
            case "is_active" -> "isActive";
            default -> field;
        };
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Specification<User> andIfPresent(Specification<User> base, Specification<User> other) {
        if (other == null) {
            return base;
        }
        return base == null ? other : base.and(other);
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
            default ->
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid account_type: " + accountType);
        };
    }

    private String toAccountType(Role role) {
        return switch (role) {
            case FIRM_ADMIN -> "law_firm_admin";
            case INDEPENDENT -> "independent_advocate";
            default -> role.name().toLowerCase();
        };
    }

    private UserSummaryResponse toSummary(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getName())
                .accountType(toAccountType(user.getRole()))
                .firmName(user.getFirm() != null ? user.getFirm().getName() : null)
                .barNumber(user.getBarRegistrationNumber())
                .phone(user.getPhone())
                .address(formatAddress(user))
                .specialization(user.getAreaOfSpecialization())
                .isActive(user.isActive())
                .createdDate(user.getCreatedDate())
                .build();
    }

    private String formatAddress(User user) {
        String raw = String.join(", ",
                nullSafe(user.getStreetAddress()),
                nullSafe(user.getCity()),
                nullSafe(user.getState()),
                nullSafe(user.getPinCode()));
        return raw.replaceAll("(, )+", ", ").replaceAll("^, |, $", "").trim();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
