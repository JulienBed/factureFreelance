package com.facture.domain.event;

import java.time.LocalDateTime;

/**
 * Base class for all invoice-related domain events
 */
public abstract class InvoiceEvent {

    public final Long invoiceId;
    public final Long userId;
    public final Long tenantId;
    public final LocalDateTime occurredAt;

    protected InvoiceEvent(Long invoiceId, Long userId, Long tenantId) {
        this.invoiceId = invoiceId;
        this.userId = userId;
        this.tenantId = tenantId;
        this.occurredAt = LocalDateTime.now();
    }
}
