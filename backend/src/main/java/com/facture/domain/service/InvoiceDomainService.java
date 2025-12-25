package com.facture.domain.service;

import com.facture.domain.repository.InvoiceRepository;
import com.facture.entity.Invoice;
import com.facture.entity.InvoiceStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;

/**
 * Domain service containing core business logic for invoices
 * This service is stateless and focuses on domain rules
 */
@ApplicationScoped
public class InvoiceDomainService {

    @Inject
    InvoiceRepository invoiceRepository;

    /**
     * Generate invoice number following business rules
     * Format: FACT-{TENANT_ID}-YYYY-MM-XXXXX
     */
    public String generateInvoiceNumber(Long tenantId) {
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        long count = invoiceRepository.countByTenantIdAndMonth(tenantId, year, month);
        return String.format("FACT-%d-%d-%02d-%05d", tenantId, year, month, count + 1);
    }

    /**
     * Check if an invoice is overdue based on business rules
     */
    public boolean isInvoiceOverdue(Invoice invoice) {
        return invoice.status == InvoiceStatus.SENT
            && invoice.dueDate.isBefore(LocalDate.now())
            && invoice.paidDate == null;
    }

    /**
     * Update invoice status based on business rules
     */
    public void updateInvoiceStatus(Invoice invoice) {
        if (isInvoiceOverdue(invoice)) {
            invoice.status = InvoiceStatus.OVERDUE;
        }
    }

    /**
     * Mark invoice as paid with business rule validation
     */
    public void markAsPaid(Invoice invoice, LocalDate paidDate) {
        if (invoice.status == InvoiceStatus.PAID) {
            throw new IllegalStateException("Invoice is already paid");
        }
        invoice.status = InvoiceStatus.PAID;
        invoice.paidDate = paidDate != null ? paidDate : LocalDate.now();
    }

    /**
     * Mark invoice as sent with business rule validation
     */
    public void markAsSent(Invoice invoice) {
        if (invoice.status == InvoiceStatus.PAID) {
            throw new IllegalStateException("Cannot send a paid invoice");
        }
        if (invoice.status == InvoiceStatus.SENT || invoice.status == InvoiceStatus.OVERDUE) {
            throw new IllegalStateException("Invoice is already sent");
        }
        invoice.status = InvoiceStatus.SENT;
    }

    /**
     * Check if invoice needs a payment reminder
     */
    public boolean needsPaymentReminder(Invoice invoice, int daysBeforeDue) {
        if (invoice.status != InvoiceStatus.SENT) {
            return false;
        }
        LocalDate reminderDate = invoice.dueDate.minusDays(daysBeforeDue);
        return LocalDate.now().isAfter(reminderDate) || LocalDate.now().isEqual(reminderDate);
    }
}
