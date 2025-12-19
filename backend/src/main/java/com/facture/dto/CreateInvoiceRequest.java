package com.facture.dto;

import com.facture.entity.InvoiceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CreateInvoiceRequest {

    @NotNull(message = "Client ID is required")
    public Long clientId;

    @NotNull(message = "Issue date is required")
    public LocalDate issueDate;

    @NotNull(message = "Due date is required")
    public LocalDate dueDate;

    public InvoiceStatus status;

    public BigDecimal taxRate;

    public String notes;

    public String paymentTerms;

    @NotEmpty(message = "At least one item is required")
    @Valid
    public List<InvoiceItemDto> items;
}
