package com.facture.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CreateClientRequest {

    @NotBlank(message = "Company name is required")
    public String companyName;

    public String contactName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    public String email;

    public String phone;
    public String addressStreet;
    public String addressCity;
    public String addressPostalCode;
    public String addressCountry;
    public String siret;
    public String tvaNumber;
    public String notes;
}
