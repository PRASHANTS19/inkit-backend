package com.inkit.backend.firm;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FirmService {

    @Autowired
    private FirmRepository firmRepository;

    public Firm createFirm(Firm firm) {
        return firmRepository.save(firm);
    }
    
    public Firm updateFirm(UUID id, Firm updatedFirm) {
        Firm existingFirm = firmRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Firm not found with id: " + id));
        existingFirm.setName(updatedFirm.getName());
        return firmRepository.save(existingFirm);
    }
    public void deleteFirm(UUID id){
        firmRepository.deleteById(id);
    }

    public List<Firm> getAllFirms() {
        return firmRepository.findAll();
    }   

    public Firm getFirmById(UUID id) {
        return firmRepository.findById(id).orElse(null);
    }
}
