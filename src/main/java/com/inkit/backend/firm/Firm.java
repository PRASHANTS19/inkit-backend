package com.inkit.backend.firm;
import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;

@Entity
@Table(name = "firms")
@Builder
public class Firm {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;

    public Firm() {}
    public Firm(String name) {
        this.name = name;
    }
    public Firm(UUID id, String name) {
        this.id = id;
        this.name = name;
    }
    public UUID getID(){
        return id;
    }
    public void setID(UUID id){
        this.id = id;
    }   
    public String getName() {
        return name;
    }   
    public void setName(String name) {
        this.name = name;
    }
}
