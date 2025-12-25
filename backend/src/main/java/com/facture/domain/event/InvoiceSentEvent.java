package com.facture.domain.event;

import java.time.LocalDate;

/**
 * Event fired when an invoice is sent to a client
 */
public class InvoiceSentEvent extends InvoiceEvent {

    public final String invoiceNumber;
    public final String clientEmail;
    public final String clientName;
    public final LocalDate dueDate;

    public InvoiceSentEvent(Long invoiceId, Long userId, Long tenantId,
                           String invoiceNumber, String clientEmail,
                           String clientName, LocalDate dueDate) {
        super(invoiceId, userId, tenantId);
        this.invoiceNumber = invoiceNumber;
        this.clientEmail = clientEmail;
        this.clientName = clientName;
        this.dueDate = dueDate;
    }
}
