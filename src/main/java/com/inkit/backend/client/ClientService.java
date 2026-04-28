package com.inkit.backend.client;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.inkit.backend.auth.User;
import com.inkit.backend.auth.UserRepository;
import com.inkit.backend.common.enums.Role;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    public Client createClient(String userEmail, Client request) {
        User user = getUserByEmail(userEmail);

        Client client = clientRepository.findByEmail(request.getEmail())
                .orElseGet(() -> clientRepository.save(Client.builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .phoneNumber(request.getPhoneNumber())
                        .streetAddress(request.getStreetAddress())
                        .city(request.getCity())
                        .state(request.getState())
                        .pinCode(request.getPinCode())
                        .role(Role.CLIENT)
                        .build()));

        if (!user.getClients().contains(client)) {
            user.getClients().add(client);
            userRepository.save(user);
        }

        return client;
    }

    public List<Client> getClients(String userEmail) {
        User user = getUserByEmail(userEmail);
        return clientRepository.findByUsersId(user.getId());
    }

    public Client getClientById(String userEmail, UUID clientId) {
        User user = getUserByEmail(userEmail);
        ensureAssignedToUser(user, clientId);
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));
    }

    public Client updateClient(String userEmail, UUID clientId, Client request) {
        User user = getUserByEmail(userEmail);
        ensureAssignedToUser(user, clientId);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        client.setName(request.getName());
        client.setEmail(request.getEmail());
        client.setPhoneNumber(request.getPhoneNumber());
        client.setStreetAddress(request.getStreetAddress());
        client.setCity(request.getCity());
        client.setState(request.getState());
        client.setPinCode(request.getPinCode());
        client.setRole(Role.CLIENT);

        return clientRepository.save(client);
    }

    public void deleteClient(String userEmail, UUID clientId) {
        User user = getUserByEmail(userEmail);
        ensureAssignedToUser(user, clientId);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        user.getClients().remove(client);
        userRepository.save(user);

        if (client.getUsers().isEmpty()) {
            clientRepository.delete(client);
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private void ensureAssignedToUser(User user, UUID clientId) {
        if (!clientRepository.existsByIdAndUsersId(clientId, user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this client");
        }
    }
}
