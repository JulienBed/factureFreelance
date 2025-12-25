package com.facture.scheduler;

import com.facture.entity.Invoice;
import com.facture.entity.InvoiceStatus;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Scheduled job to aggregate invoice statistics
 * Runs every night at 1 AM
 */
@ApplicationScoped
public class StatsAggregationJob {

    /**
     * Aggregate invoice statistics every night at 1 AM
     * Calculates daily, monthly, and yearly statistics
     */
    @Scheduled(cron = "0 0 1 * * ?") // Every day at 1 AM
    @Transactional
    public void aggregateDailyStats() {
        Log.info("📊 [Job] Starting daily stats aggregation job...");

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate yearStart = today.withDayOfYear(1);

        // Aggregate by tenant (for multi-tenant support)
        List<Long> tenantIds = Invoice.find("SELECT DISTINCT user.tenantId FROM Invoice")
            .project(Long.class)
            .list();

        for (Long tenantId : tenantIds) {
            try {
                aggregateTenantStats(tenantId, today, monthStart, yearStart);
            } catch (Exception e) {
                Log.errorf(e, "❌ Failed to aggregate stats for tenant %d", tenantId);
            }
        }

        Log.info("📊 [Job] Daily stats aggregation complete");
    }

    private void aggregateTenantStats(Long tenantId, LocalDate today, LocalDate monthStart, LocalDate yearStart) {
        // Today's stats
        StatsSnapshot todayStats = calculateStats(tenantId, today, today.plusDays(1));
        Log.infof("📊 Tenant %d - Today: %d invoices, %.2f EUR revenue",
            tenantId, todayStats.count, todayStats.revenue);

        // This month's stats
        StatsSnapshot monthStats = calculateStats(tenantId, monthStart, monthStart.plusMonths(1));
        Log.infof("📊 Tenant %d - This month: %d invoices, %.2f EUR revenue",
            tenantId, monthStats.count, monthStats.revenue);

        // This year's stats
        StatsSnapshot yearStats = calculateStats(tenantId, yearStart, yearStart.plusYears(1));
        Log.infof("📊 Tenant %d - This year: %d invoices, %.2f EUR revenue",
            tenantId, yearStats.count, yearStats.revenue);

        // TODO: Persist these stats to a dedicated stats table for fast retrieval
        // This would enable dashboard analytics without recalculating every time
    }

    private StatsSnapshot calculateStats(Long tenantId, LocalDate from, LocalDate to) {
        // Count invoices
        long totalCount = Invoice.count(
            "user.tenantId = ?1 and createdAt >= ?2 and createdAt < ?3",
            tenantId, from.atStartOfDay(), to.atStartOfDay()
        );

        // Count by status
        long draftCount = Invoice.count(
            "user.tenantId = ?1 and status = ?2 and createdAt >= ?3 and createdAt < ?4",
            tenantId, InvoiceStatus.DRAFT, from.atStartOfDay(), to.atStartOfDay()
        );

        long sentCount = Invoice.count(
            "user.tenantId = ?1 and status = ?2 and createdAt >= ?3 and createdAt < ?4",
            tenantId, InvoiceStatus.SENT, from.atStartOfDay(), to.atStartOfDay()
        );

        long paidCount = Invoice.count(
            "user.tenantId = ?1 and status = ?2 and createdAt >= ?3 and createdAt < ?4",
            tenantId, InvoiceStatus.PAID, from.atStartOfDay(), to.atStartOfDay()
        );

        long overdueCount = Invoice.count(
            "user.tenantId = ?1 and status = ?2 and createdAt >= ?3 and createdAt < ?4",
            tenantId, InvoiceStatus.OVERDUE, from.atStartOfDay(), to.atStartOfDay()
        );

        // Calculate revenue (paid invoices only)
        List<Invoice> paidInvoices = Invoice.list(
            "user.tenantId = ?1 and status = ?2 and paidDate >= ?3 and paidDate < ?4",
            tenantId, InvoiceStatus.PAID, from, to
        );

        BigDecimal revenue = paidInvoices.stream()
            .map(invoice -> invoice.total)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate pending amount (sent + overdue, not paid)
        List<Invoice> pendingInvoices = Invoice.list(
            "user.tenantId = ?1 and (status = ?2 or status = ?3) and createdAt >= ?4 and createdAt < ?5",
            tenantId, InvoiceStatus.SENT, InvoiceStatus.OVERDUE, from.atStartOfDay(), to.atStartOfDay()
        );

        BigDecimal pending = pendingInvoices.stream()
            .map(invoice -> invoice.total)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new StatsSnapshot(totalCount, draftCount, sentCount, paidCount, overdueCount, revenue, pending);
    }

    /**
     * Simple stats snapshot class
     */
    private static class StatsSnapshot {
        final long count;
        final long draftCount;
        final long sentCount;
        final long paidCount;
        final long overdueCount;
        final BigDecimal revenue;
        final BigDecimal pending;

        StatsSnapshot(long count, long draftCount, long sentCount, long paidCount,
                     long overdueCount, BigDecimal revenue, BigDecimal pending) {
            this.count = count;
            this.draftCount = draftCount;
            this.sentCount = sentCount;
            this.paidCount = paidCount;
            this.overdueCount = overdueCount;
            this.revenue = revenue;
            this.pending = pending;
        }
    }
}
