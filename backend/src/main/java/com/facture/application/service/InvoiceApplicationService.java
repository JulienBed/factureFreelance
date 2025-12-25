package com.facture.application.service;

import com.facture.domain.event.InvoiceCreatedEvent;
import com.facture.domain.event.InvoicePaidEvent;
import com.facture.domain.event.InvoiceSentEvent;
import com.facture.domain.repository.InvoiceRepository;
import com.facture.domain.service.InvoiceDomainService;
import com.facture.dto.CreateInvoiceRequest;
import com.facture.dto.InvoiceDto;
import com.facture.entity.*;
import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application service orchestrating use cases and transactions
 * Coordinates between domain services, repositories, and events
 */
@ApplicationScoped
public class InvoiceApplicationService {

    @Inject
    InvoiceRepository invoiceRepository;

    @Inject
    InvoiceDomainService domainService;

    @Inject
    Event<InvoiceCreatedEvent> invoiceCreatedEvent;

    @Inject
    Event<InvoiceSentEvent> invoiceSentEvent;

    @Inject
    Event<InvoicePaidEvent> invoicePaidEvent;

    @Transactional
    public InvoiceDto createInvoice(Long userId, CreateInvoiceRequest request) {
        User user = User.findById(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        Client client = Client.findById(request.clientId);
        if (client == null || !client.user.id.equals(userId)) {
            throw new NotFoundException("Client not found");
        }

        Invoice invoice = new Invoice();
        invoice.user = user;
        invoice.client = client;
        invoice.invoiceNumber = domainService.generateInvoiceNumber(user.tenantId);
        updateInvoiceFromRequest(invoice, request);

        // Add items
        request.items.forEach(itemDto -> {
            InvoiceItem item = itemDto.toEntity();
            invoice.addItem(item);
        });

        invoice.calculateTotals();
        invoiceRepository.save(invoice);

        // Fire domain event
        invoiceCreatedEvent.fireAsync(new InvoiceCreatedEvent(
            invoice.id,
            userId,
            user.tenantId,
            invoice.invoiceNumber,
            client.name,
            invoice.total
        ));

        return InvoiceDto.fromEntity(invoice);
    }

    public List<InvoiceDto> getInvoicesByTenantId(Long userId, InvoiceStatus status) {
        User user = User.findById(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        List<Invoice> invoices;
        if (status != null) {
            invoices = invoiceRepository.findByTenantIdAndStatus(user.tenantId, status);
        } else {
            invoices = invoiceRepository.findByTenantId(user.tenantId);
        }

        // Filter by userId for additional security
        return invoices.stream()
            .filter(inv -> inv.user.id.equals(userId))
            .map(InvoiceDto::fromEntity)
            .collect(Collectors.toList());
    }

    public InvoiceDto getInvoiceById(Long userId, Long invoiceId) {
        User user = User.findById(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        Invoice invoice = invoiceRepository.findById(invoiceId, user.tenantId)
            .orElseThrow(() -> new NotFoundException("Invoice not found"));

        if (!invoice.user.id.equals(userId)) {
            throw new NotFoundException("Invoice not found");
        }

        return InvoiceDto.fromEntity(invoice);
    }

    @Transactional
    public InvoiceDto updateInvoice(Long userId, Long invoiceId, CreateInvoiceRequest request) {
        User user = User.findById(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        Invoice invoice = invoiceRepository.findById(invoiceId, user.tenantId)
            .orElseThrow(() -> new NotFoundException("Invoice not found"));

        if (!invoice.user.id.equals(userId)) {
            throw new NotFoundException("Invoice not found");
        }

        Client client = Client.findById(request.clientId);
        if (client == null || !client.user.id.equals(userId)) {
            throw new NotFoundException("Client not found");
        }

        invoice.client = client;
        updateInvoiceFromRequest(invoice, request);

        // Update items
        invoice.items.clear();
        request.items.forEach(itemDto -> {
            InvoiceItem item = itemDto.toEntity();
            invoice.addItem(item);
        });

        invoice.calculateTotals();
        invoiceRepository.save(invoice);

        return InvoiceDto.fromEntity(invoice);
    }

    @Transactional
    public void deleteInvoice(Long userId, Long invoiceId) {
        User user = User.findById(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        Invoice invoice = invoiceRepository.findById(invoiceId, user.tenantId)
            .orElseThrow(() -> new NotFoundException("Invoice not found"));

        if (!invoice.user.id.equals(userId)) {
            throw new NotFoundException("Invoice not found");
        }

        invoiceRepository.delete(invoice);
    }

    @Transactional
    public InvoiceDto updateInvoiceStatus(Long userId, Long invoiceId, InvoiceStatus status) {
        User user = User.findById(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        Invoice invoice = invoiceRepository.findById(invoiceId, user.tenantId)
            .orElseThrow(() -> new NotFoundException("Invoice not found"));

        if (!invoice.user.id.equals(userId)) {
            throw new NotFoundException("Invoice not found");
        }

        InvoiceStatus previousStatus = invoice.status;

        // Apply domain logic for status changes
        if (status == InvoiceStatus.PAID) {
            domainService.markAsPaid(invoice, LocalDate.now());
        } else if (status == InvoiceStatus.SENT) {
            domainService.markAsSent(invoice);
        } else {
            invoice.status = status;
        }

        invoiceRepository.save(invoice);

        // Fire appropriate domain events
        if (status == InvoiceStatus.PAID && previousStatus != InvoiceStatus.PAID) {
            invoicePaidEvent.fireAsync(new InvoicePaidEvent(
                invoice.id,
                userId,
                user.tenantId,
                invoice.invoiceNumber,
                invoice.total,
                invoice.paidDate
            ));
        } else if (status == InvoiceStatus.SENT && previousStatus != InvoiceStatus.SENT) {
            invoiceSentEvent.fireAsync(new InvoiceSentEvent(
                invoice.id,
                userId,
                user.tenantId,
                invoice.invoiceNumber,
                invoice.client.email,
                invoice.client.name,
                invoice.dueDate
            ));
        }

        return InvoiceDto.fromEntity(invoice);
    }

    private void updateInvoiceFromRequest(Invoice invoice, CreateInvoiceRequest request) {
        invoice.issueDate = request.issueDate;
        invoice.dueDate = request.dueDate;
        invoice.status = request.status != null ? request.status : InvoiceStatus.DRAFT;
        invoice.taxRate = request.taxRate != null ? request.taxRate : new BigDecimal("20.00");
        invoice.notes = request.notes;
        invoice.paymentTerms = request.paymentTerms;
    }
}
