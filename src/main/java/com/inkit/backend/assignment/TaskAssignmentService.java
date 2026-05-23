package com.inkit.backend.assignment;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.inkit.backend.assignment.dto.AssignmentFilterRequest;
import com.inkit.backend.assignment.dto.TaskAssignmentResponse;
import com.inkit.backend.assignment.dto.TaskAssignmentUpsertRequest;
import com.inkit.backend.auth.User;
import com.inkit.backend.auth.UserRepository;
import com.inkit.backend.common.enums.Role;
import com.inkit.backend.task.Task;
import com.inkit.backend.task.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskAssignmentService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public List<TaskAssignmentResponse> list(String userEmail, String taskIdFilter, String userIdFilter) {
        User user = getUserByEmail(userEmail);
        return taskRepository.findAll().stream()
                .filter(task -> task.getFirm() != null && user.getFirm() != null
                        && task.getFirm().getID().equals(user.getFirm().getID()))
                .filter(task -> task.getAssignedTo() != null)
                .filter(task -> taskIdFilter == null || taskIdFilter.equals(String.valueOf(task.getId())))
                .filter(task -> userIdFilter == null || userIdFilter.equals(String.valueOf(task.getAssignedTo().getId())))
                .map(this::toResponse)
                .toList();
    }

    public List<TaskAssignmentResponse> filter(String userEmail, AssignmentFilterRequest request) {
        List<TaskAssignmentResponse> rows = list(userEmail, null, null);
        Map<String, Object> filter = request != null ? request.getFilter() : null;
        if (filter == null || filter.isEmpty()) return rows;

        String taskId = stringValue(filter.get("task_id"));
        String caseId = stringValue(filter.get("case_id"));
        String assignedToUserId = stringValue(filter.get("assigned_to_user_id"));

        return rows.stream()
                .filter(r -> taskId == null || taskId.equals(String.valueOf(r.getTaskId())))
                .filter(r -> caseId == null || caseId.equals(String.valueOf(r.getCaseId())))
                .filter(r -> assignedToUserId == null || assignedToUserId.equals(String.valueOf(r.getAssignedToUserId())))
                .toList();
    }

    private TaskAssignmentResponse toResponse(Task task) {
        return TaskAssignmentResponse.builder()
                .id(task.getId())
                .taskId(task.getId())
                .caseId(task.getCaseRef() != null ? task.getCaseRef().getId() : null)
                .assignedToUserId(task.getAssignedTo() != null ? task.getAssignedTo().getId() : null)
                .assignmentDate(task.getUpdatedDate())
                .build();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public TaskAssignmentResponse create(String userEmail, TaskAssignmentUpsertRequest request) {
        User user = getUserByEmail(userEmail);
        ensureRole(user, Role.FIRM_ADMIN, Role.INDEPENDENT);
        if (request.getTaskId() == null || request.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "task_id and user_id are required");
        }

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        User assignee = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        task.setAssignedTo(assignee);
        return toResponse(taskRepository.save(task));
    }

    public void delete(String userEmail, UUID taskId) {
        User user = getUserByEmail(userEmail);
        ensureRole(user, Role.FIRM_ADMIN, Role.INDEPENDENT);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        task.setAssignedTo(null);
        taskRepository.save(task);
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private void ensureRole(User user, Role... allowed) {
        for (Role role : allowed) {
            if (user.getRole() == role) return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permission");
    }
}
