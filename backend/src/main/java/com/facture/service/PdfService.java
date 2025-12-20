package com.facture.service;

import com.facture.entity.Invoice;
import com.facture.entity.InvoiceItem;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.jboss.logging.Logger;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA1;
import org.mustangproject.Item;
import org.mustangproject.Product;
import org.mustangproject.TradeParty;
import org.mustangproject.Contact;
import org.mustangproject.BankDetails;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class PdfService {

    private static final Logger logger = Logger.getLogger(PdfService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generateFacturXPdf(Invoice invoice) throws IOException {
        logger.infof("Generating Factur-X PDF for invoice: %s", invoice.invoiceNumber);

        // 1. Create visual PDF/A-1
        byte[] visualPdf = createVisualPdf(invoice);

        // 2. Create Mustangproject Invoice object
        org.mustangproject.Invoice mustangInvoice = createMustangInvoice(invoice);

        // 3. Create Factur-X PDF/A-3 with embedded XML using ZUGFeRDExporterFromA1
        ZUGFeRDExporterFromA1 exporter = new ZUGFeRDExporterFromA1();
        exporter.load(visualPdf);
        exporter.setZUGFeRDVersion(2);
        exporter.setProfile("EN16931");
        exporter.setTransaction(mustangInvoice);

        // 5. Export to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.export(outputStream);

        logger.infof("Factur-X PDF generated successfully for invoice: %s", invoice.invoiceNumber);
        return outputStream.toByteArray();
    }

    private byte[] createVisualPdf(Invoice invoice) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        // Load fonts once - PDFBox 3.x: use PDType1Font static methods
        // In PDFBox 3.x, fonts are accessed via PDType1Font static fields
        PDType1Font helveticaBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font helvetica = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        float margin = 50;
        float yPosition = page.getMediaBox().getHeight() - margin;
        float fontSize = 12;

        // Header - Company info
        contentStream.beginText();
        contentStream.setFont(helveticaBold, 18);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText(invoice.user.companyName != null ? invoice.user.companyName : invoice.user.getFullName());
        contentStream.endText();

        yPosition -= 20;
        contentStream.beginText();
        contentStream.setFont(helvetica, 10);
        contentStream.newLineAtOffset(margin, yPosition);
        if (invoice.user.addressStreet != null) {
            contentStream.showText(invoice.user.addressStreet);
        }
        contentStream.endText();

        yPosition -= 15;
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        if (invoice.user.addressPostalCode != null && invoice.user.addressCity != null) {
            contentStream.showText(invoice.user.addressPostalCode + " " + invoice.user.addressCity);
        }
        contentStream.endText();

        yPosition -= 15;
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        if (invoice.user.siret != null) {
            contentStream.showText("SIRET: " + invoice.user.siret);
        }
        contentStream.endText();

        // Invoice title and number
        yPosition -= 40;
        contentStream.beginText();
        contentStream.setFont(helveticaBold, 20);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("FACTURE");
        contentStream.endText();

        yPosition -= 25;
        contentStream.beginText();
        contentStream.setFont(helveticaBold, fontSize);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("N° " + invoice.invoiceNumber);
        contentStream.endText();

        // Client info
        yPosition -= 30;
        contentStream.beginText();
        contentStream.setFont(helveticaBold, fontSize);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Client:");
        contentStream.endText();

        yPosition -= 15;
        contentStream.beginText();
        contentStream.setFont(helvetica, fontSize);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText(invoice.client.companyName);
        contentStream.endText();

        if (invoice.client.addressStreet != null) {
            yPosition -= 15;
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(invoice.client.addressStreet);
            contentStream.endText();
        }

        if (invoice.client.addressPostalCode != null && invoice.client.addressCity != null) {
            yPosition -= 15;
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(invoice.client.addressPostalCode + " " + invoice.client.addressCity);
            contentStream.endText();
        }

        // Dates
        yPosition -= 30;
        contentStream.beginText();
        contentStream.setFont(helvetica, fontSize);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Date d'emission: " + invoice.issueDate.format(DATE_FORMATTER));
        contentStream.endText();

        yPosition -= 15;
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Date d'echeance: " + invoice.dueDate.format(DATE_FORMATTER));
        contentStream.endText();

        // Items table header
        yPosition -= 40;
        float tableMargin = margin;

        contentStream.beginText();
        contentStream.setFont(helveticaBold, 10);
        contentStream.newLineAtOffset(tableMargin, yPosition);
        contentStream.showText("Description");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(tableMargin + 250, yPosition);
        contentStream.showText("Qte");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(tableMargin + 300, yPosition);
        contentStream.showText("P.U. HT");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(tableMargin + 380, yPosition);
        contentStream.showText("TVA");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(tableMargin + 430, yPosition);
        contentStream.showText("Total HT");
        contentStream.endText();

        // Line under header
        yPosition -= 5;
        contentStream.moveTo(tableMargin, yPosition);
        contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
        contentStream.stroke();

        // Items
        yPosition -= 20;
        contentStream.setFont(helvetica, 10);

        for (InvoiceItem item : invoice.items) {
            contentStream.beginText();
            contentStream.newLineAtOffset(tableMargin, yPosition);
            // Truncate description if too long
            String desc = item.description.length() > 35 ? item.description.substring(0, 35) + "..." : item.description;
            contentStream.showText(desc);
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(tableMargin + 250, yPosition);
            contentStream.showText(item.quantity.stripTrailingZeros().toPlainString());
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(tableMargin + 300, yPosition);
            contentStream.showText(String.format("%.2f EUR", item.unitPrice));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(tableMargin + 380, yPosition);
            contentStream.showText(item.taxRate.stripTrailingZeros().toPlainString() + "%");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(tableMargin + 430, yPosition);
            contentStream.showText(String.format("%.2f EUR", item.amount));
            contentStream.endText();

            yPosition -= 20;
        }

        // Totals
        yPosition -= 20;
        float totalsX = page.getMediaBox().getWidth() - margin - 150;

        contentStream.beginText();
        contentStream.setFont(helvetica, 10);
        contentStream.newLineAtOffset(totalsX, yPosition);
        contentStream.showText("Total HT:");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(totalsX + 80, yPosition);
        contentStream.showText(String.format("%.2f EUR", invoice.subtotal));
        contentStream.endText();

        yPosition -= 15;
        contentStream.beginText();
        contentStream.newLineAtOffset(totalsX, yPosition);
        contentStream.showText("TVA (" + invoice.taxRate.stripTrailingZeros().toPlainString() + "%):");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(totalsX + 80, yPosition);
        contentStream.showText(String.format("%.2f EUR", invoice.taxAmount));
        contentStream.endText();

        yPosition -= 15;
        contentStream.beginText();
        contentStream.setFont(helveticaBold, 12);
        contentStream.newLineAtOffset(totalsX, yPosition);
        contentStream.showText("Total TTC:");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(totalsX + 80, yPosition);
        contentStream.showText(String.format("%.2f EUR", invoice.total));
        contentStream.endText();

        // Payment info
        if (invoice.user.iban != null) {
            yPosition -= 40;
            contentStream.beginText();
            contentStream.setFont(helveticaBold, 10);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Informations de paiement:");
            contentStream.endText();

            yPosition -= 15;
            contentStream.beginText();
            contentStream.setFont(helvetica, 9);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("IBAN: " + invoice.user.iban);
            contentStream.endText();

            if (invoice.user.bic != null) {
                yPosition -= 12;
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("BIC: " + invoice.user.bic);
                contentStream.endText();
            }
        }

        // Notes
        if (invoice.notes != null && !invoice.notes.isEmpty()) {
            yPosition -= 30;
            contentStream.beginText();
            contentStream.setFont(helveticaBold, 10);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Notes:");
            contentStream.endText();

            yPosition -= 15;
            contentStream.beginText();
            contentStream.setFont(helvetica, 9);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(invoice.notes);
            contentStream.endText();
        }

        contentStream.close();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        document.close();

        return baos.toByteArray();
    }

    private org.mustangproject.Invoice createMustangInvoice(com.facture.entity.Invoice invoice) {
        // Create sender (TradeParty)
        TradeParty sender = new TradeParty(
            invoice.user.companyName != null ? invoice.user.companyName : invoice.user.getFullName(),
            invoice.user.addressStreet != null ? invoice.user.addressStreet : "",
            invoice.user.addressPostalCode != null ? invoice.user.addressPostalCode : "",
            invoice.user.addressCity != null ? invoice.user.addressCity : "",
            invoice.user.addressCountry != null ? invoice.user.addressCountry : "FR"
        );

        if (invoice.user.siret != null) {
            sender.addTaxID(invoice.user.siret);
            sender.addVATID(invoice.user.siret);
        }

        // Add contact information
        if (invoice.user.email != null || invoice.user.phone != null) {
            String contactName = invoice.user.getFullName();
            String phone = invoice.user.phone != null ? invoice.user.phone : "";
            String email = invoice.user.email != null ? invoice.user.email : "";
            sender.setContact(new Contact(contactName, phone, email));
        }

        // Add bank details if available
        if (invoice.user.iban != null) {
            String bic = invoice.user.bic != null ? invoice.user.bic : "";
            sender.addBankDetails(new BankDetails(invoice.user.iban, bic));
        }

        // Create recipient (TradeParty)
        TradeParty recipient = new TradeParty(
            invoice.client.companyName,
            invoice.client.addressStreet != null ? invoice.client.addressStreet : "",
            invoice.client.addressPostalCode != null ? invoice.client.addressPostalCode : "",
            invoice.client.addressCity != null ? invoice.client.addressCity : "",
            invoice.client.addressCountry != null ? invoice.client.addressCountry : "FR"
        );

        if (invoice.client.siret != null) {
            recipient.addTaxID(invoice.client.siret);
        }

        // Create Mustang Invoice
        org.mustangproject.Invoice mustangInvoice = new org.mustangproject.Invoice()
            .setDueDate(java.sql.Date.valueOf(invoice.dueDate))
            .setIssueDate(java.sql.Date.valueOf(invoice.issueDate))
            .setDeliveryDate(java.sql.Date.valueOf(invoice.issueDate)) // Use issue date as delivery date if not available
            .setSender(sender)
            .setRecipient(recipient)
            .setNumber(invoice.invoiceNumber);

        // Add items
        for (InvoiceItem item : invoice.items) {
            // Product code "C62" = "Unit" (UN/ECE Recommendation 20)
            Product product = new Product(
                item.description,
                "", // description additionnelle
                "C62", // unit code (C62 = Unit)
                item.taxRate
            );
            
            Item mustangItem = new Item(
                product,
                item.unitPrice,
                item.quantity
            );
            
            mustangInvoice.addItem(mustangItem);
        }

        // Payment terms can be set via setPaymentTerms(IZUGFeRDPaymentTerms) if needed
        // For now, we skip it as it requires creating an IZUGFeRDPaymentTerms object

        return mustangInvoice;
    }
}
