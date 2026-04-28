package com.inkit.backend.client;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public Client createClient(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Client request) {
        return clientService.createClient(userDetails.getUsername(), request);
    }

    @GetMapping
    public List<Client> getClients(
            @AuthenticationPrincipal UserDetails userDetails) {
        return clientService.getClients(userDetails.getUsername());
    }

    @GetMapping("/{clientId}")
    public Client getClientById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID clientId) {
        return clientService.getClientById(userDetails.getUsername(), clientId);
    }

    @PutMapping("/{clientId}")
    public Client updateClient(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID clientId,
            @RequestBody Client request) {
        return clientService.updateClient(userDetails.getUsername(), clientId, request);
    }

    @DeleteMapping("/{clientId}")
    public String deleteClient(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID clientId) {
        clientService.deleteClient(userDetails.getUsername(), clientId);
        return "Client deleted successfully";
    }
}
