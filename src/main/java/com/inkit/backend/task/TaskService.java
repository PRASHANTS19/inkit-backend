package com.inkit.backend.task;

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
import com.inkit.backend.common.enums.Priority;
import com.inkit.backend.common.enums.Role;
import com.inkit.backend.common.enums.Status;
import com.inkit.backend.task.dto.TaskFilterRequest;
import com.inkit.backend.task.dto.TaskResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public List<TaskResponse> listTasks(String userEmail, String caseId, String status, String assignedTo,
            String sortBy, Integer limit) {
        User user = getUserByEmail(userEmail);

        Specification<Task> spec = roleScopeSpec(user);
        spec = andIfPresent(spec, uuidEquals("caseRef", caseId));
        spec = andIfPresent(spec, enumEquals("status", status, Status.class));
        spec = andIfPresent(spec, uuidEquals("assignedTo", assignedTo));

        Pageable pageable = PageRequest.of(0, sanitizeLimit(limit), parseSort(sortBy));
        return taskRepository.findAll(spec, pageable).getContent().stream().map(this::toResponse).toList();
    }

    public List<TaskResponse> filterTasks(String userEmail, TaskFilterRequest request) {
        User user = getUserByEmail(userEmail);

        Specification<Task> spec = roleScopeSpec(user);
        spec = andIfPresent(spec, filterMapSpec(request.getFilter()));
        Pageable pageable = PageRequest.of(0, sanitizeLimit(request.getLimit()), parseSort(request.getSortBy()));

        return taskRepository.findAll(spec, pageable).getContent().stream().map(this::toResponse).toList();
    }

    public TaskResponse getTaskById(String userEmail, UUID taskId) {
        User user = getUserByEmail(userEmail);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (!isAccessible(user, task)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this task");
        }

        return toResponse(task);
    }

    public TaskResponse createTask(String userEmail, Task request) {
        User user = getUserByEmail(userEmail);
        request.setId(null);
        if (request.getFirm() == null) {
            request.setFirm(user.getFirm());
        }
        return toResponse(taskRepository.save(request));
    }

    public TaskResponse updateTask(String userEmail, UUID taskId, Task request) {
        User user = getUserByEmail(userEmail);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (!isAccessible(user, task)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot update this task");
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());
        task.setTaskType(request.getTaskType());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setActualHours(request.getActualHours());
        task.setNotes(request.getNotes());
        task.setReminderSent(request.getReminderSent());
        task.setCaseRef(request.getCaseRef());
        task.setAssignedTo(request.getAssignedTo());
        task.setFirm(request.getFirm() != null ? request.getFirm() : task.getFirm());

        return toResponse(taskRepository.save(task));
    }

    public void deleteTask(String userEmail, UUID taskId) {
        User user = getUserByEmail(userEmail);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (!isAccessible(user, task)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot delete this task");
        }

        taskRepository.delete(task);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private Specification<Task> roleScopeSpec(User user) {
        if (user.getRole() == Role.ASSOCIATE) {
            return (root, query, cb) -> cb.equal(root.get("assignedTo").get("id"), user.getId());
        }
        return null;
    }

    private boolean isAccessible(User user, Task task) {
        if (user.getRole() == Role.ASSOCIATE) {
            return task.getAssignedTo() != null && user.getId().equals(task.getAssignedTo().getId());
        }
        return true;
    }

    private Specification<Task> filterMapSpec(Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) {
            return null;
        }

        List<Specification<Task>> specs = new ArrayList<>();
        specs.add(uuidEquals("caseRef", stringValue(filter.get("case_id"))));
        specs.add(enumEquals("status", stringValue(filter.get("status")), Status.class));
        specs.add(uuidEquals("assignedTo", stringValue(filter.get("assigned_to"))));
        specs.add(enumEquals("priority", stringValue(filter.get("priority")), Priority.class));

        String title = stringValue(filter.get("title"));
        if (title != null && !title.isBlank()) {
            specs.add(containsIgnoreCase("title", title));
        }

        Specification<Task> result = null;
        for (Specification<Task> spec : specs) {
            if (spec == null) {
                continue;
            }
            result = result == null ? Specification.where(spec) : result.and(spec);
        }
        return result;
    }

    private Specification<Task> containsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String like = "%" + value.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(field)), like);
    }

    private Specification<Task> uuidEquals(String relationField, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid UUID for " + relationField + ": " + value);
        }

        return (root, query, cb) -> cb.equal(root.get(relationField).get("id"), uuid);
    }

    private <T extends Enum<T>> Specification<Task> enumEquals(String field, String value, Class<T> enumType) {
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

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Specification<Task> andIfPresent(Specification<Task> base, Specification<Task> other) {
        if (base == null) {
            return other;
        }
        return other == null ? base : base.and(other);
    }

    private int sanitizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 50;
        }
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
            case "due_date" -> "dueDate";
            case "assigned_to" -> "assignedTo";
            default -> field;
        };
    }

    private TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus() != null ? task.getStatus().name() : null)
                .priority(task.getPriority() != null ? task.getPriority().name() : null)
                .dueDate(task.getDueDate())
                .taskType(task.getTaskType())
                .estimatedHours(task.getEstimatedHours())
                .actualHours(task.getActualHours())
                .notes(task.getNotes())
                .reminderSent(task.getReminderSent())
                .createdDate(task.getCreatedDate())
                .caseId(task.getCaseRef() != null ? task.getCaseRef().getId() : null)
                .caseTitle(task.getCaseRef() != null ? task.getCaseRef().getCaseTitle() : null)
                .caseNumber(task.getCaseRef() != null ? task.getCaseRef().getCaseNumber() : null)
                .assigneeId(task.getAssignedTo() != null ? task.getAssignedTo().getId() : null)
                .assigneeName(task.getAssignedTo() != null ? task.getAssignedTo().getName() : null)
                .assigneeEmail(task.getAssignedTo() != null ? task.getAssignedTo().getEmail() : null)
                .build();
    }
}
