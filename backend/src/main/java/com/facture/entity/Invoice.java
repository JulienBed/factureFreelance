package com.facture.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
public class Invoice extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    public Client client;

    @Column(name = "invoice_number", unique = true, nullable = false)
    public String invoiceNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public InvoiceStatus status = InvoiceStatus.DRAFT;

    @NotNull
    @Column(name = "issue_date", nullable = false)
    public LocalDate issueDate;

    @NotNull
    @Column(name = "due_date", nullable = false)
    public LocalDate dueDate;

    @Column(name = "paid_date")
    public LocalDate paidDate;

    @NotNull
    @Column(precision = 10, scale = 2, nullable = false)
    public BigDecimal subtotal = BigDecimal.ZERO;

    @NotNull
    @Column(name = "tax_rate", precision = 5, scale = 2, nullable = false)
    public BigDecimal taxRate = new BigDecimal("20.00"); // TVA 20% par défaut

    @NotNull
    @Column(name = "tax_amount", precision = 10, scale = 2, nullable = false)
    public BigDecimal taxAmount = BigDecimal.ZERO;

    @NotNull
    @Column(precision = 10, scale = 2, nullable = false)
    public BigDecimal total = BigDecimal.ZERO;

    @Column(length = 3, nullable = false)
    public String currency = "EUR";

    @Column(length = 2000)
    public String notes;

    @Column(name = "payment_terms", length = 500)
    public String paymentTerms;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    public List<InvoiceItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Reminder> reminders = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (invoiceNumber == null) {
            generateInvoiceNumber();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotals();
        updateStatus();
    }

    public void generateInvoiceNumber() {
        // Format: FACT-YYYY-MM-XXXXX
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        long count = Invoice.count("invoiceNumber like ?1", String.format("FACT-%d-%02d%%", year, month));
        this.invoiceNumber = String.format("FACT-%d-%02d-%05d", year, month, count + 1);
    }

    public void calculateTotals() {
        subtotal = items.stream()
                .map(item -> item.amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        taxAmount = subtotal.multiply(taxRate).divide(new BigDecimal("100"));
        total = subtotal.add(taxAmount);
    }

    public void updateStatus() {
        if (status == InvoiceStatus.SENT && dueDate.isBefore(LocalDate.now()) && paidDate == null) {
            status = InvoiceStatus.OVERDUE;
        }
    }

    public void addItem(InvoiceItem item) {
        items.add(item);
        item.invoice = this;
        item.orderIndex = items.size() - 1;
    }

    public static List<Invoice> findByUserId(Long userId) {
        return list("user.id", userId);
    }

    public static List<Invoice> findByUserIdAndStatus(Long userId, InvoiceStatus status) {
        return list("user.id = ?1 and status = ?2", userId, status);
    }

    public static List<Invoice> findOverdueInvoices() {
        return list("status = ?1 and dueDate < ?2", InvoiceStatus.SENT, LocalDate.now());
    }

    public boolean isOverdue() {
        return status == InvoiceStatus.SENT && dueDate.isBefore(LocalDate.now());
    }
}
