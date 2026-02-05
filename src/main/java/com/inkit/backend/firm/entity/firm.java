package com.inkit.backend.firm.entity;
import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class firm {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
}
