package com.facture.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
public class InvoiceItem extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    public Invoice invoice;

    @NotBlank(message = "Description is required")
    @Column(nullable = false, length = 500)
    public String description;

    @NotNull(message = "Quantity is required")
    @Column(precision = 10, scale = 2, nullable = false)
    public BigDecimal quantity;

    @NotNull(message = "Unit price is required")
    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    public BigDecimal unitPrice;

    @NotNull
    @Column(name = "tax_rate", precision = 5, scale = 2, nullable = false)
    public BigDecimal taxRate = new BigDecimal("20.00");

    @NotNull
    @Column(precision = 10, scale = 2, nullable = false)
    public BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "order_index")
    public Integer orderIndex = 0;

    @PrePersist
    @PreUpdate
    public void calculateAmount() {
        amount = quantity.multiply(unitPrice);
    }
}
