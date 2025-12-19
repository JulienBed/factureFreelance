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
import org.mustangproject.ZUGFeRD.*;
import org.mustangproject.Product;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@ApplicationScoped
public class PdfService {

    private static final Logger logger = Logger.getLogger(PdfService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generateFacturXPdf(Invoice invoice) throws IOException {
        logger.infof("Generating Factur-X PDF for invoice: %s", invoice.invoiceNumber);

        // 1. Create visual PDF
        byte[] visualPdf = createVisualPdf(invoice);

        // 2. Create Factur-X metadata
        ZUGFeRDExporterFromPDF exporter = new ZUGFeRDExporterFromPDF();

        // Load the visual PDF
        exporter.load(visualPdf);

        // 3. Set Factur-X metadata
        exporter.setZUGFeRDVersion(2);
        exporter.setProfile("EN16931"); // Norme européenne EN 16931

        // Create transaction data
        IZUGFeRDExportableTransaction transaction = createFacturXTransaction(invoice);
        exporter.setTransaction(transaction);

        // 4. Generate Factur-X PDF (PDF/A-3 with embedded XML)
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

        float margin = 50;
        float yPosition = page.getMediaBox().getHeight() - margin;
        float fontSize = 12;

        // Header - Company info
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText(invoice.user.companyName != null ? invoice.user.companyName : invoice.user.getFullName());
        contentStream.endText();

        yPosition -= 20;
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
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
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("FACTURE");
        contentStream.endText();

        yPosition -= 25;
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), fontSize);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("N° " + invoice.invoiceNumber);
        contentStream.endText();

        // Client info
        yPosition -= 30;
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), fontSize);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Client:");
        contentStream.endText();

        yPosition -= 15;
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), fontSize);
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
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), fontSize);
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
        float tableTop = yPosition;
        float tableMargin = margin;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
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
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);

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
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
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
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
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
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Informations de paiement:");
            contentStream.endText();

            yPosition -= 15;
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
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
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Notes:");
            contentStream.endText();

            yPosition -= 15;
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
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

    private IZUGFeRDExportableTransaction createFacturXTransaction(Invoice invoice) {
        return new IZUGFeRDExportableTransaction() {
            @Override
            public String getNumber() {
                return invoice.invoiceNumber;
            }

            @Override
            public Date getIssueDate() {
                return java.sql.Date.valueOf(invoice.issueDate);
            }

            @Override
            public Date getDueDate() {
                return java.sql.Date.valueOf(invoice.dueDate);
            }

            @Override
            public String getCurrency() {
                return invoice.currency;
            }

            @Override
            public IZUGFeRDExportableItem[] getZUGFeRDExportableItems() {
                return invoice.items.stream()
                        .map(item -> (IZUGFeRDExportableItem) new IZUGFeRDExportableItem() {
                            @Override
                            public BigDecimal getPrice() {
                                return item.unitPrice;
                            }

                            @Override
                            public BigDecimal getQuantity() {
                                return item.quantity;
                            }

                            @Override
                            public String getProduct() {
                                return new Product(item.description, "", "HUR", item.taxRate);
                            }
                        })
                        .toArray(IZUGFeRDExportableItem[]::new);
            }

            @Override
            public String getOwnOrganisationName() {
                return invoice.user.companyName != null ? invoice.user.companyName : invoice.user.getFullName();
            }

            @Override
            public String getOwnOrganisationFullPlaintextInfo() {
                StringBuilder info = new StringBuilder();
                info.append(getOwnOrganisationName()).append("\n");
                if (invoice.user.addressStreet != null) {
                    info.append(invoice.user.addressStreet).append("\n");
                }
                if (invoice.user.addressPostalCode != null && invoice.user.addressCity != null) {
                    info.append(invoice.user.addressPostalCode).append(" ").append(invoice.user.addressCity).append("\n");
                }
                if (invoice.user.siret != null) {
                    info.append("SIRET: ").append(invoice.user.siret).append("\n");
                }
                if (invoice.user.email != null) {
                    info.append("Email: ").append(invoice.user.email);
                }
                return info.toString();
            }

            @Override
            public String getRecipientName() {
                return invoice.client.companyName;
            }

            @Override
            public String getRecipient() {
                StringBuilder info = new StringBuilder();
                info.append(invoice.client.companyName).append("\n");
                if (invoice.client.addressStreet != null) {
                    info.append(invoice.client.addressStreet).append("\n");
                }
                if (invoice.client.addressPostalCode != null && invoice.client.addressCity != null) {
                    info.append(invoice.client.addressPostalCode).append(" ").append(invoice.client.addressCity).append("\n");
                }
                if (invoice.client.siret != null) {
                    info.append("SIRET: ").append(invoice.client.siret);
                }
                return info.toString();
            }

            @Override
            public String getOwnTaxID() {
                return invoice.user.siret;
            }

            @Override
            public String getOwnVATID() {
                return invoice.user.siret; // In France, SIRET can be used
            }

            @Override
            public IZUGFeRDAllowanceCharge[] getZUGFeRDAllowanceCharges() {
                return new IZUGFeRDAllowanceCharge[0];
            }

            @Override
            public IZUGFeRDPaymentTerms getPaymentTerms() {
                if (invoice.paymentTerms != null && !invoice.paymentTerms.isEmpty()) {
                    return new IZUGFeRDPaymentTerms(invoice.paymentTerms, getDueDate());
                }
                return null;
            }

            @Override
            public String getReferenceNumber() {
                return invoice.invoiceNumber;
            }
        };
    }
}
