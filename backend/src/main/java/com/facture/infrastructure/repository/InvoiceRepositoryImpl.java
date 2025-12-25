package com.facture.infrastructure.repository;

import com.facture.domain.repository.InvoiceRepository;
import com.facture.entity.Invoice;
import com.facture.entity.InvoiceStatus;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Infrastructure implementation of InvoiceRepository using Panache
 */
@ApplicationScoped
public class InvoiceRepositoryImpl implements InvoiceRepository {

    @Override
    public Invoice save(Invoice invoice) {
        invoice.persist();
        return invoice;
    }

    @Override
    public Optional<Invoice> findById(Long id, Long tenantId) {
        return Invoice.find("id = ?1 and user.tenantId = ?2", id, tenantId)
            .firstResultOptional();
    }

    @Override
    public List<Invoice> findByTenantId(Long tenantId) {
        return Invoice.list("user.tenantId", tenantId);
    }

    @Override
    public List<Invoice> findByTenantIdAndStatus(Long tenantId, InvoiceStatus status) {
        return Invoice.list("user.tenantId = ?1 and status = ?2", tenantId, status);
    }

    @Override
    public List<Invoice> findOverdueInvoices() {
        return Invoice.list("status = ?1 and dueDate < ?2 and paidDate is null",
            InvoiceStatus.SENT, LocalDate.now());
    }

    @Override
    public List<Invoice> findInvoicesDueForReminder(LocalDate reminderDate) {
        return Invoice.list("status = ?1 and dueDate = ?2 and paidDate is null",
            InvoiceStatus.SENT, reminderDate);
    }

    @Override
    public void delete(Invoice invoice) {
        invoice.delete();
    }

    @Override
    public long countByTenantIdAndMonth(Long tenantId, int year, int month) {
        String pattern = String.format("FACT-%d-%d-%02d%%", tenantId, year, month);
        return Invoice.count("invoiceNumber like ?1 and user.tenantId = ?2", pattern, tenantId);
    }
}
