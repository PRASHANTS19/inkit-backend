package com.inkit.backend.hearing.dto;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HearingFilterRequest {
    private Map<String, Object> filter;
    private String sortBy;
    private Integer limit;
}

