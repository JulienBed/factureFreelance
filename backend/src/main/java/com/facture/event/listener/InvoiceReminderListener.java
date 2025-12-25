package com.facture.event.listener;

import com.facture.domain.event.InvoiceSentEvent;
import com.facture.entity.Invoice;
import com.facture.entity.Reminder;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

/**
 * Listener for invoice events to create automatic reminders
 */
@ApplicationScoped
public class InvoiceReminderListener {

    @Transactional
    public void onInvoiceSent(@ObservesAsync InvoiceSentEvent event) {
        Log.infof("🔔 [Reminder] Scheduling reminders for invoice: %s (due: %s)",
            event.invoiceNumber, event.dueDate);

        Invoice invoice = Invoice.findById(event.invoiceId);
        if (invoice == null) {
            Log.warnf("Invoice %d not found for reminder scheduling", event.invoiceId);
            return;
        }

        // Create initial reminder entries
        // Reminder 1: 7 days before due date
        createReminder(invoice, event.dueDate.atStartOfDay().minusDays(7), "7 days before due date");

        // Reminder 2: 3 days before due date
        createReminder(invoice, event.dueDate.atStartOfDay().minusDays(3), "3 days before due date");

        // Reminder 3: On due date
        createReminder(invoice, event.dueDate.atStartOfDay(), "On due date");

        Log.infof("✅ Created 3 automatic reminders for invoice %s", event.invoiceNumber);
    }

    private void createReminder(Invoice invoice, LocalDateTime scheduledAt, String description) {
        if (scheduledAt.isAfter(LocalDateTime.now())) {
            Reminder reminder = new Reminder();
            reminder.invoice = invoice;
            reminder.scheduledAt = scheduledAt;
            reminder.sent = false;
            reminder.description = description;
            reminder.persist();
        }
    }
}
