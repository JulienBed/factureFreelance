package com.facture.domain.event;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Event fired when an invoice is marked as paid
 */
public class InvoicePaidEvent extends InvoiceEvent {

    public final String invoiceNumber;
    public final BigDecimal amount;
    public final LocalDate paidDate;

    public InvoicePaidEvent(Long invoiceId, Long userId, Long tenantId,
                           String invoiceNumber, BigDecimal amount, LocalDate paidDate) {
        super(invoiceId, userId, tenantId);
        this.invoiceNumber = invoiceNumber;
        this.amount = amount;
        this.paidDate = paidDate;
    }
}
