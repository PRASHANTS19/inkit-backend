package com.inkit.backend.client;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inkit.backend.auth.User;
import com.inkit.backend.common.enums.Role;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "client")
public class Client {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private String email;
    private String phoneNumber;
    private String streetAddress;
    private String city;
    private String state;
    private String pinCode;

    @Enumerated(EnumType.STRING)
    private Role role;

    @JsonIgnore
    @Builder.Default
    @ManyToMany(mappedBy = "clients")
    private Set<User> users = new HashSet<>();
}
