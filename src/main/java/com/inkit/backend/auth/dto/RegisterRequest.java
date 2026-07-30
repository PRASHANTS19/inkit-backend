package com.inkit.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    /** Accepted values: independent_advocate, law_firm_admin, associate */
    private String accountType;

    private String fullName;
    private String firmName;
    private String barNumber;
}
