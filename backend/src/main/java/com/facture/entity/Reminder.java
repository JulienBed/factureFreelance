package com.facture.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reminders")
public class Reminder extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    public Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ReminderType type = ReminderType.AUTO;

    @Column(name = "sent_at", nullable = false)
    public LocalDateTime sentAt;

    @Column(name = "sent_by")
    public String sentBy;

    @Column(length = 2000)
    public String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ReminderStatus status = ReminderStatus.SENT;

    @PrePersist
    public void prePersist() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }

    public static List<Reminder> findByInvoiceId(Long invoiceId) {
        return list("invoice.id", invoiceId);
    }

    public static long countByInvoiceId(Long invoiceId) {
        return count("invoice.id", invoiceId);
    }

    public enum ReminderType {
        AUTO,
        MANUAL
    }

    public enum ReminderStatus {
        SENT,
        FAILED
    }
}
