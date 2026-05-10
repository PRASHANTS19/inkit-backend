package com.inkit.backend.task;

import java.util.List;
import java.util.UUID;

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

import com.inkit.backend.task.dto.TaskFilterRequest;
import com.inkit.backend.task.dto.TaskResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public List<TaskResponse> listTasks(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, name = "case_id") String caseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, name = "assigned_to") String assignedTo,
            @RequestParam(defaultValue = "-created_date") String sortBy,
            @RequestParam(defaultValue = "50") Integer limit) {
        return taskService.listTasks(userDetails.getUsername(), caseId, status, assignedTo, sortBy, limit);
    }

    @PostMapping("/filter")
    public List<TaskResponse> filterTasks(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody TaskFilterRequest request) {
        return taskService.filterTasks(userDetails.getUsername(), request);
    }

    @GetMapping("/{id}")
    public TaskResponse getTaskById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return taskService.getTaskById(userDetails.getUsername(), id);
    }

    @PostMapping
    public TaskResponse createTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Task request) {
        return taskService.createTask(userDetails.getUsername(), request);
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestBody Task request) {
        return taskService.updateTask(userDetails.getUsername(), id, request);
    }

    @DeleteMapping("/{id}")
    public String deleteTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        taskService.deleteTask(userDetails.getUsername(), id);
        return "Task deleted successfully";
    }
}
