package com.inkit.backend.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum CaseType {
    Civil,
    Criminal,
    Corporate,
    Family,
    Commercial,
    Labour,
    Tax,
    Constitutional,
    Writ,
    Other;

    @JsonCreator
    public static CaseType fromString(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().replace('-', '_').replace(' ', '_').toUpperCase();
        if (normalized.equals("LABOR")) {
            return Labour;
        }
        return Enum.valueOf(CaseType.class, normalized);
    }
}
