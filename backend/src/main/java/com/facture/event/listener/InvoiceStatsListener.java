package com.facture.event.listener;

import com.facture.domain.event.InvoiceCreatedEvent;
import com.facture.domain.event.InvoicePaidEvent;
import com.facture.domain.event.InvoiceSentEvent;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;

/**
 * Listener for invoice events to update statistics
 */
@ApplicationScoped
public class InvoiceStatsListener {

    public void onInvoiceCreated(@ObservesAsync InvoiceCreatedEvent event) {
        Log.infof("📊 [Stats] Invoice created for tenant %d: %s (%.2f EUR)",
            event.tenantId, event.invoiceNumber, event.total);

        // TODO: Update statistics
        // - Increment invoice count for tenant
        // - Update total invoiced amount
        // - Update monthly/yearly aggregates
    }

    public void onInvoiceSent(@ObservesAsync InvoiceSentEvent event) {
        Log.infof("📊 [Stats] Invoice sent for tenant %d: %s",
            event.tenantId, event.invoiceNumber);

        // TODO: Update statistics
        // - Increment sent invoice count
        // - Track average time from creation to send
    }

    public void onInvoicePaid(@ObservesAsync InvoicePaidEvent event) {
        Log.infof("📊 [Stats] Invoice paid for tenant %d: %s (%.2f EUR on %s)",
            event.tenantId, event.invoiceNumber, event.amount, event.paidDate);

        // TODO: Update statistics
        // - Increment paid invoice count
        // - Add to revenue total
        // - Track payment delays
        // - Update cash flow projections
    }
}
