package com.inkit.backend.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Court {
    High_Court,
    Supreme_Court,
    District_Court,
    Session_Court,
    Megistrate_Court,
    Tribunal,
    Family_Court;

    @JsonCreator
    public static Court fromString(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().replace('-', '_').replace(' ', '_').toUpperCase();
        if (normalized.equals("SESSIONS_COURT") || normalized.equals("SESSION_COURT") || normalized.equals("SESSIONSCOURT") || normalized.equals("SESSIONCOURT")) {
            return Session_Court;
        }
        if (normalized.equals("MAGISTRATE_COURT") || normalized.equals("MEGISTRATE_COURT") || normalized.equals("MAGISTRATECOURT") || normalized.equals("MEGISTRATECOURT")) {
            return Megistrate_Court;
        }
        return Enum.valueOf(Court.class, normalized);
    }
}
