package com.inkit.backend.invoice.dto;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceFilterRequest {
    private Map<String, Object> filter;
    private String sortBy;
    private Integer limit;
}

