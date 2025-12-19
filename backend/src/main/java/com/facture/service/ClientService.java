package com.facture.service;

import com.facture.dto.ClientDto;
import com.facture.dto.CreateClientRequest;
import com.facture.entity.Client;
import com.facture.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ClientService {

    @Transactional
    public ClientDto createClient(Long userId, CreateClientRequest request) {
        User user = User.findById(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        Client client = new Client();
        client.user = user;
        updateClientFromRequest(client, request);
        client.persist();

        return ClientDto.fromEntity(client);
    }

    public List<ClientDto> getClientsByUserId(Long userId, String search) {
        List<Client> clients;
        if (search != null && !search.isEmpty()) {
            clients = Client.findByUserIdWithSearch(userId, search);
        } else {
            clients = Client.findByUserId(userId);
        }
        return clients.stream().map(ClientDto::fromEntity).collect(Collectors.toList());
    }

    public ClientDto getClientById(Long userId, Long clientId) {
        Client client = Client.findById(clientId);
        if (client == null || !client.user.id.equals(userId)) {
            throw new NotFoundException("Client not found");
        }
        return ClientDto.fromEntity(client);
    }

    @Transactional
    public ClientDto updateClient(Long userId, Long clientId, CreateClientRequest request) {
        Client client = Client.findById(clientId);
        if (client == null || !client.user.id.equals(userId)) {
            throw new NotFoundException("Client not found");
        }

        updateClientFromRequest(client, request);
        client.persist();

        return ClientDto.fromEntity(client);
    }

    @Transactional
    public void deleteClient(Long userId, Long clientId) {
        Client client = Client.findById(clientId);
        if (client == null || !client.user.id.equals(userId)) {
            throw new NotFoundException("Client not found");
        }
        client.delete();
    }

    private void updateClientFromRequest(Client client, CreateClientRequest request) {
        client.companyName = request.companyName;
        client.contactName = request.contactName;
        client.email = request.email;
        client.phone = request.phone;
        client.addressStreet = request.addressStreet;
        client.addressCity = request.addressCity;
        client.addressPostalCode = request.addressPostalCode;
        client.addressCountry = request.addressCountry;
        client.siret = request.siret;
        client.tvaNumber = request.tvaNumber;
        client.notes = request.notes;
    }
}
