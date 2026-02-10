package com.inkit.backend.auth.dto;

import lombok.*;
import java.util.*;
import com.inkit.backend.common.enums.Role;
import com.inkit.backend.firm.Firm;
@Getter
@Setter
@NoArgsConstructor  
@AllArgsConstructor
@Builder
public class UserDto {
    private UUID id;
    private String name;
    private String email;
    private String YOE;
    private String areaOfSpecialization;
    private String streetAddress;
    private String city;
    private String state;
    private String pinCode;
    private Role role;
    private Firm firm;
}
