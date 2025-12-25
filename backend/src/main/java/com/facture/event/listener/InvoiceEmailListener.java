package com.facture.event.listener;

import com.facture.domain.event.InvoiceCreatedEvent;
import com.facture.domain.event.InvoicePaidEvent;
import com.facture.domain.event.InvoiceSentEvent;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;

/**
 * Listener for invoice events to send email notifications
 */
@ApplicationScoped
public class InvoiceEmailListener {

    public void onInvoiceCreated(@ObservesAsync InvoiceCreatedEvent event) {
        Log.infof("📧 [Email] Invoice created: %s for client %s (amount: %.2f EUR)",
            event.invoiceNumber, event.clientName, event.total);

        // TODO: Implement email sending logic
        // - Send confirmation email to user
        // - Include invoice number, client name, total amount
    }

    public void onInvoiceSent(@ObservesAsync InvoiceSentEvent event) {
        Log.infof("📧 [Email] Invoice sent: %s to %s (due: %s)",
            event.invoiceNumber, event.clientEmail, event.dueDate);

        // TODO: Implement email sending logic
        // - Send invoice PDF to client
        // - Include payment instructions
        // - CC the user
    }

    public void onInvoicePaid(@ObservesAsync InvoicePaidEvent event) {
        Log.infof("📧 [Email] Invoice paid: %s (amount: %.2f EUR, paid on: %s)",
            event.invoiceNumber, event.amount, event.paidDate);

        // TODO: Implement email sending logic
        // - Send payment confirmation to user
        // - Send thank you email to client
    }
}
