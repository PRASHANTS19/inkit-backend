package com.inkit.backend.assignment;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.inkit.backend.assignment.dto.AssignmentFilterRequest;
import com.inkit.backend.assignment.dto.TaskAssignmentResponse;
import com.inkit.backend.assignment.dto.TaskAssignmentUpsertRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/assignments/tasks")
@RequiredArgsConstructor
public class TaskAssignmentController {

    private final TaskAssignmentService taskAssignmentService;

    @GetMapping
    public List<TaskAssignmentResponse> list(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, name = "task_id") String taskId,
            @RequestParam(required = false, name = "user_id") String userId) {
        return taskAssignmentService.list(userDetails.getUsername(), taskId, userId);
    }

    @PostMapping("/filter")
    public List<TaskAssignmentResponse> filter(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AssignmentFilterRequest request) {
        return taskAssignmentService.filter(userDetails.getUsername(), request);
    }

    @PostMapping
    public TaskAssignmentResponse create(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody TaskAssignmentUpsertRequest request) {
        return taskAssignmentService.create(userDetails.getUsername(), request);
    }

    @DeleteMapping("/{id}")
    public String delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        taskAssignmentService.delete(userDetails.getUsername(), id);
        return "Task assignment removed";
    }
}
