package com.facture.dto;

import com.facture.entity.InvoiceItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class InvoiceItemDto {

    public Long id;

    @NotBlank(message = "Description is required")
    public String description;

    @NotNull(message = "Quantity is required")
    public BigDecimal quantity;

    @NotNull(message = "Unit price is required")
    public BigDecimal unitPrice;

    @NotNull
    public BigDecimal taxRate;

    public BigDecimal amount;

    public static InvoiceItemDto fromEntity(InvoiceItem item) {
        InvoiceItemDto dto = new InvoiceItemDto();
        dto.id = item.id;
        dto.description = item.description;
        dto.quantity = item.quantity;
        dto.unitPrice = item.unitPrice;
        dto.taxRate = item.taxRate;
        dto.amount = item.amount;
        return dto;
    }

    public InvoiceItem toEntity() {
        InvoiceItem item = new InvoiceItem();
        item.description = this.description;
        item.quantity = this.quantity;
        item.unitPrice = this.unitPrice;
        item.taxRate = this.taxRate != null ? this.taxRate : new BigDecimal("20.00");
        return item;
    }
}
