package com.inkit.backend.case_mgmt;

import com.inkit.backend.case_mgmt.dto.ECourtResponse;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class CaseSyncService {

    private final RestTemplate restTemplate;

    @Value("${ecourt.api.url:https://webapi.ecourtsindia.com/api/partner/case/}")
    private String apiUrl;

    @Value("${ecourt.api.token}")
    private String apiToken;

    /**
     * Fetches case details from e-Court API.
     * In a real app, you'd wrap this in CaseService to check local DB first.
     */
    public ECourtResponse fetchFromECourt(String cnrNumber) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            return restTemplate.exchange(
                    apiUrl + cnrNumber,
                    HttpMethod.GET,
                    entity,
                    ECourtResponse.class
            ).getBody();
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE, "Failed to fetch data from e-Court API", e);
        }
    }

    public boolean shouldSyncFromEcourt(Case caseData){
        if(caseData.getCnrNumber()==null || caseData.getCnrNumber().isEmpty()){
            return false; // No CNR, can't sync
        }

        LocalDateTime lastSync = caseData.getLastEcourtSync();
        if(lastSync==null || Duration.between(lastSync, LocalDateTime.now()).toHours() > 24){
            return true; // Sync if never synced or last sync was over 24 hours ago
        }

        return !lastSync.toLocalDate().equals(LocalDate.now());
    }
}