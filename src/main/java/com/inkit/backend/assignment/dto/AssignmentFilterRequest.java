package com.inkit.backend.assignment.dto;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignmentFilterRequest {
    private Map<String, Object> filter;
    private String sortBy;
    private Integer limit;
}

