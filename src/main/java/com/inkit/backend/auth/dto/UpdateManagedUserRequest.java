package com.inkit.backend.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateManagedUserRequest {
    private String name;
    private String barRegistrationNumber;
    private String phone;
    private String YOE;
    private String areaOfSpecialization;
    private String streetAddress;
    private String city;
    private String state;
    private String pinCode;
    private Boolean isActive;
}
