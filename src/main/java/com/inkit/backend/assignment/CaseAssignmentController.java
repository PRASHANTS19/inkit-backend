package com.inkit.backend.assignment;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inkit.backend.assignment.dto.AssignmentFilterRequest;
import com.inkit.backend.assignment.dto.CaseAssignmentResponse;
import com.inkit.backend.assignment.dto.CaseAssignmentUpsertRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/assignments/cases")
@RequiredArgsConstructor
public class CaseAssignmentController {

    private final CaseAssignmentService caseAssignmentService;

    @GetMapping
    public List<CaseAssignmentResponse> list(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, name = "case_id") String caseId,
            @RequestParam(required = false, name = "user_id") String userId,
            @RequestParam(defaultValue = "-assignment_date") String sortBy,
            @RequestParam(defaultValue = "50") Integer limit) {
        return caseAssignmentService.list(userDetails.getUsername(), caseId, userId, sortBy, limit);
    }

    @PostMapping("/filter")
    public List<CaseAssignmentResponse> filter(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AssignmentFilterRequest request) {
        return caseAssignmentService.filter(userDetails.getUsername(), request);
    }

    @PostMapping
    public CaseAssignmentResponse create(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CaseAssignmentUpsertRequest request) {
        return caseAssignmentService.create(userDetails.getUsername(), request);
    }

    @DeleteMapping("/{id}")
    public String delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        caseAssignmentService.delete(userDetails.getUsername(), id);
        return "Case assignment deleted successfully";
    }
}
