package com.inkit.backend.firm;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/firms")
public class FirmController {
    
    @Autowired
    private FirmService firmService;

    @PostMapping
    public ResponseEntity<Firm> createFirm(Firm firm) {
        Firm created = firmService.createFirm(firm);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Firm> updateFirm(@PathVariable UUID id, Firm updatedFirm) {
        Firm updated = firmService.updateFirm(id, updatedFirm);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<Firm>> getAllFirm() {
        List<Firm> firms = firmService.getAllFirms();
        return ResponseEntity.ok(firms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Firm> getFirmById(@PathVariable UUID id) {
        Firm firm = firmService.getFirmById(id);
        return firm!=null ? ResponseEntity.ok(firm) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFirm(@PathVariable UUID id) {
        firmService.deleteFirm(id);
        return ResponseEntity.noContent().build();
    }
}
