package com.inkit.backend.case_mgmt.dto;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaseFilterRequest {
    private Map<String, Object> filter;
    private String sortBy = "-createdDate";
    private Integer limit = 50;
}
