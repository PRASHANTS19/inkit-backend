package com.inkit.backend.task.dto;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskFilterRequest {
    private Map<String, Object> filter;
    private String sortBy = "-created_date";
    private Integer limit = 50;
}
