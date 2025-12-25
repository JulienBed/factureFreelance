package com.facture.scheduler;

import com.facture.entity.Invoice;
import com.facture.entity.InvoiceStatus;
import com.facture.entity.Reminder;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job to send automatic payment reminders
 * Runs every hour to check for pending reminders
 */
@ApplicationScoped
public class InvoiceReminderJob {

    /**
     * Send payment reminders every hour
     * Checks for unsent reminders that are due
     */
    @Scheduled(cron = "0 0 * * * ?") // Every hour at minute 0
    @Transactional
    public void sendPendingReminders() {
        Log.info("🔔 [Job] Starting payment reminder job...");

        LocalDateTime now = LocalDateTime.now();

        // Find all unsent reminders that are due
        List<Reminder> pendingReminders = Reminder.list(
            "sent = false and scheduledAt <= ?1",
            now
        );

        if (pendingReminders.isEmpty()) {
            Log.info("✅ No pending reminders to send");
            return;
        }

        int sentCount = 0;
        for (Reminder reminder : pendingReminders) {
            try {
                Invoice invoice = reminder.invoice;

                // Skip if invoice is already paid
                if (invoice.status == InvoiceStatus.PAID) {
                    reminder.sent = true;
                    reminder.sentAt = now;
                    Log.infof("⏭️ Skipping reminder for paid invoice: %s", invoice.invoiceNumber);
                    continue;
                }

                // Send reminder email
                sendReminderEmail(invoice, reminder);

                // Mark as sent
                reminder.sent = true;
                reminder.sentAt = now;
                sentCount++;

                Log.infof("✅ Sent reminder for invoice %s to %s (%s)",
                    invoice.invoiceNumber, invoice.client.email, reminder.description);

            } catch (Exception e) {
                Log.errorf(e, "❌ Failed to send reminder for invoice %d", reminder.invoice.id);
            }
        }

        Log.infof("🔔 [Job] Payment reminder job complete. Sent %d reminders", sentCount);
    }

    /**
     * Update overdue invoices every day at 2 AM
     * Marks SENT invoices as OVERDUE if past due date
     */
    @Scheduled(cron = "0 0 2 * * ?") // Every day at 2 AM
    @Transactional
    public void updateOverdueInvoices() {
        Log.info("⏰ [Job] Starting overdue invoice update job...");

        List<Invoice> overdueInvoices = Invoice.list(
            "status = ?1 and dueDate < ?2 and paidDate is null",
            InvoiceStatus.SENT,
            LocalDate.now()
        );

        if (overdueInvoices.isEmpty()) {
            Log.info("✅ No invoices to mark as overdue");
            return;
        }

        int count = 0;
        for (Invoice invoice : overdueInvoices) {
            invoice.status = InvoiceStatus.OVERDUE;
            count++;
            Log.infof("⚠️ Marked invoice %s as OVERDUE (due: %s)",
                invoice.invoiceNumber, invoice.dueDate);
        }

        Log.infof("⏰ [Job] Overdue invoice update complete. Marked %d invoices as overdue", count);
    }

    private void sendReminderEmail(Invoice invoice, Reminder reminder) {
        // TODO: Implement actual email sending
        // For now, just log the action
        Log.infof("📧 Would send reminder email to %s for invoice %s",
            invoice.client.email, invoice.invoiceNumber);

        // Email should include:
        // - Invoice number and amount
        // - Due date
        // - Payment instructions
        // - Link to invoice PDF
    }
}
