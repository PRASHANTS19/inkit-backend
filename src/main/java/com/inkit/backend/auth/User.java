package com.inkit.backend.auth;
import java.util.UUID;
import jakarta.annotation.Generated;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.inkit.backend.common.enums.Role;
import com.inkit.backend.firm.Firm;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private String email;
    private String password;
    private String barRegistrationNumber;
    private String YOE;
    private String areaOfSpecialization;
    private String streetAddress;
    private String city;
    private String state;
    private String pinCode;

    @ManyToOne
    private Firm firm;
}
