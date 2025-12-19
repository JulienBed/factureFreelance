package com.facture.dto;

import com.facture.entity.Invoice;
import com.facture.entity.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceDto {

    public Long id;
    public Long clientId;
    public String clientName;
    public String invoiceNumber;
    public InvoiceStatus status;
    public LocalDate issueDate;
    public LocalDate dueDate;
    public LocalDate paidDate;
    public BigDecimal subtotal;
    public BigDecimal taxRate;
    public BigDecimal taxAmount;
    public BigDecimal total;
    public String currency;
    public String notes;
    public String paymentTerms;
    public List<InvoiceItemDto> items;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public static InvoiceDto fromEntity(Invoice invoice) {
        InvoiceDto dto = new InvoiceDto();
        dto.id = invoice.id;
        dto.clientId = invoice.client.id;
        dto.clientName = invoice.client.companyName;
        dto.invoiceNumber = invoice.invoiceNumber;
        dto.status = invoice.status;
        dto.issueDate = invoice.issueDate;
        dto.dueDate = invoice.dueDate;
        dto.paidDate = invoice.paidDate;
        dto.subtotal = invoice.subtotal;
        dto.taxRate = invoice.taxRate;
        dto.taxAmount = invoice.taxAmount;
        dto.total = invoice.total;
        dto.currency = invoice.currency;
        dto.notes = invoice.notes;
        dto.paymentTerms = invoice.paymentTerms;
        dto.items = invoice.items.stream()
                .map(InvoiceItemDto::fromEntity)
                .collect(Collectors.toList());
        dto.createdAt = invoice.createdAt;
        dto.updatedAt = invoice.updatedAt;
        return dto;
    }
}
