package com.facture.service;

import com.facture.dto.CreateInvoiceRequest;
import com.facture.dto.InvoiceDto;
import com.facture.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class InvoiceService {

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
        updateInvoiceFromRequest(invoice, request);

        // Add items
        request.items.forEach(itemDto -> {
            InvoiceItem item = itemDto.toEntity();
            invoice.addItem(item);
        });

        invoice.calculateTotals();
        invoice.persist();

        return InvoiceDto.fromEntity(invoice);
    }

    public List<InvoiceDto> getInvoicesByUserId(Long userId, InvoiceStatus status) {
        List<Invoice> invoices;
        if (status != null) {
            invoices = Invoice.findByUserIdAndStatus(userId, status);
        } else {
            invoices = Invoice.findByUserId(userId);
        }
        return invoices.stream().map(InvoiceDto::fromEntity).collect(Collectors.toList());
    }

    public InvoiceDto getInvoiceById(Long userId, Long invoiceId) {
        Invoice invoice = Invoice.findById(invoiceId);
        if (invoice == null || !invoice.user.id.equals(userId)) {
            throw new NotFoundException("Invoice not found");
        }
        return InvoiceDto.fromEntity(invoice);
    }

    @Transactional
    public InvoiceDto updateInvoice(Long userId, Long invoiceId, CreateInvoiceRequest request) {
        Invoice invoice = Invoice.findById(invoiceId);
        if (invoice == null || !invoice.user.id.equals(userId)) {
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
        invoice.persist();

        return InvoiceDto.fromEntity(invoice);
    }

    @Transactional
    public void deleteInvoice(Long userId, Long invoiceId) {
        Invoice invoice = Invoice.findById(invoiceId);
        if (invoice == null || !invoice.user.id.equals(userId)) {
            throw new NotFoundException("Invoice not found");
        }
        invoice.delete();
    }

    @Transactional
    public InvoiceDto updateInvoiceStatus(Long userId, Long invoiceId, InvoiceStatus status) {
        Invoice invoice = Invoice.findById(invoiceId);
        if (invoice == null || !invoice.user.id.equals(userId)) {
            throw new NotFoundException("Invoice not found");
        }

        invoice.status = status;
        if (status == InvoiceStatus.PAID && invoice.paidDate == null) {
            invoice.paidDate = LocalDate.now();
        }
        invoice.persist();

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
