package com.inkit.backend.auth.dto;

import lombok.*;

@Getter
@Setter
public class UpdateUserRequest {

    private String name;
    private String barRegistrationNumber;
    private String YOE;
    private String areaOfSpecialization;
    private String streetAddress;
    private String city;
    private String state;
    private String pinCode;

}
