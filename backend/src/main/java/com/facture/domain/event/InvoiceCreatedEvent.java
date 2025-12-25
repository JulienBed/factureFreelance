package com.facture.domain.event;

import java.math.BigDecimal;

/**
 * Event fired when a new invoice is created
 */
public class InvoiceCreatedEvent extends InvoiceEvent {

    public final String invoiceNumber;
    public final String clientName;
    public final BigDecimal total;

    public InvoiceCreatedEvent(Long invoiceId, Long userId, Long tenantId,
                               String invoiceNumber, String clientName, BigDecimal total) {
        super(invoiceId, userId, tenantId);
        this.invoiceNumber = invoiceNumber;
        this.clientName = clientName;
        this.total = total;
    }
}
