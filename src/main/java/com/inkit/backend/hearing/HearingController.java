package com.inkit.backend.hearing;

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

import com.inkit.backend.hearing.dto.HearingFilterRequest;
import com.inkit.backend.hearing.dto.HearingResponse;
import com.inkit.backend.hearing.dto.HearingUpsertRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hearings")
@RequiredArgsConstructor
public class HearingController {

    private final HearingService hearingService;

    @GetMapping
    public List<HearingResponse> listHearings(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, name = "case_id") String caseId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "hearing_date") String sortBy,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean upcoming) {
        return hearingService.listHearings(userDetails.getUsername(), caseId, status, sortBy, limit, upcoming);
    }

    @PostMapping("/filter")
    public List<HearingResponse> filterHearings(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody HearingFilterRequest request) {
        return hearingService.filterHearings(userDetails.getUsername(), request);
    }

    @GetMapping("/{id}")
    public HearingResponse getHearingById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return hearingService.getById(userDetails.getUsername(), id);
    }

    @PostMapping
    public HearingResponse createHearing(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody HearingUpsertRequest request) {
        return hearingService.create(userDetails.getUsername(), request);
    }

    @PutMapping("/{id}")
    public HearingResponse updateHearing(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestBody HearingUpsertRequest request) {
        return hearingService.update(userDetails.getUsername(), id, request);
    }

    @DeleteMapping("/{id}")
    public String deleteHearing(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        hearingService.delete(userDetails.getUsername(), id);
        return "Hearing deleted successfully";
    }
}
