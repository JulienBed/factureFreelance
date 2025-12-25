package com.facture.domain.repository;

import com.facture.entity.Invoice;
import com.facture.entity.InvoiceStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for Invoice aggregate
 * Follows Domain-Driven Design principles
 */
public interface InvoiceRepository {

    /**
     * Persist a new invoice
     */
    Invoice save(Invoice invoice);

    /**
     * Find invoice by ID with tenant isolation
     */
    Optional<Invoice> findById(Long id, Long tenantId);

    /**
     * Find all invoices for a tenant
     */
    List<Invoice> findByTenantId(Long tenantId);

    /**
     * Find invoices by tenant and status
     */
    List<Invoice> findByTenantIdAndStatus(Long tenantId, InvoiceStatus status);

    /**
     * Find overdue invoices across all tenants (for scheduled jobs)
     */
    List<Invoice> findOverdueInvoices();

    /**
     * Find invoices due for reminder (sent, not paid, due date approaching)
     */
    List<Invoice> findInvoicesDueForReminder(LocalDate reminderDate);

    /**
     * Delete an invoice
     */
    void delete(Invoice invoice);

    /**
     * Count invoices by tenant for current month (for invoice numbering)
     */
    long countByTenantIdAndMonth(Long tenantId, int year, int month);
}
