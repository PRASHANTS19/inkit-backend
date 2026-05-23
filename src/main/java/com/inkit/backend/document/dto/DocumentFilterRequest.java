package com.inkit.backend.document.dto;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentFilterRequest {
    private Map<String, Object> filter;
    private String sortBy;
    private Integer limit;
}

