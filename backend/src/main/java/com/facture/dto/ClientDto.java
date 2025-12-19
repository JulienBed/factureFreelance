package com.facture.dto;

import com.facture.entity.Client;

import java.time.LocalDateTime;

public class ClientDto {

    public Long id;
    public String companyName;
    public String contactName;
    public String email;
    public String phone;
    public String addressStreet;
    public String addressCity;
    public String addressPostalCode;
    public String addressCountry;
    public String siret;
    public String tvaNumber;
    public String notes;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public static ClientDto fromEntity(Client client) {
        ClientDto dto = new ClientDto();
        dto.id = client.id;
        dto.companyName = client.companyName;
        dto.contactName = client.contactName;
        dto.email = client.email;
        dto.phone = client.phone;
        dto.addressStreet = client.addressStreet;
        dto.addressCity = client.addressCity;
        dto.addressPostalCode = client.addressPostalCode;
        dto.addressCountry = client.addressCountry;
        dto.siret = client.siret;
        dto.tvaNumber = client.tvaNumber;
        dto.notes = client.notes;
        dto.createdAt = client.createdAt;
        dto.updatedAt = client.updatedAt;
        return dto;
    }
}
