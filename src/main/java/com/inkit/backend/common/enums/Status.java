package com.inkit.backend.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Status {
    ACTIVE,
    PENDING,
    CLOSED,
    ON_HOLD,
    APPEAL;

    @JsonCreator
    public static Status fromString(String value) {
        if (value == null) {
            return null;
        }
        return Enum.valueOf(Status.class, value.trim().replace('-', '_').replace(' ', '_').toUpperCase());
    }
}
