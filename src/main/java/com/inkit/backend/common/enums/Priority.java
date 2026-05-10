package com.inkit.backend.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Priority {
    HIGH,
    MEDIUM,
    LOW;

    @JsonCreator
    public static Priority fromString(String value) {
        if (value == null) {
            return null;
        }
        return Enum.valueOf(Priority.class, value.trim().replace('-', '_').replace(' ', '_').toUpperCase());
    }
}
