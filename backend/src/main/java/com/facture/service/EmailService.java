package com.facture.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;

    @Inject
    Logger logger;

    public void sendOtpEmail(String to, String otp, String firstName) {
        String subject = "Votre code de vérification - Facture Freelance";
        String body = buildOtpEmailBody(otp, firstName);

        try {
            mailer.send(Mail.withHtml(to, subject, body));
            logger.infof("OTP email sent to: %s", to);
        } catch (Exception e) {
            logger.errorf("Failed to send OTP email to %s: %s", to, e.getMessage());
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    public void sendInvoiceEmail(String to, String clientName, String invoiceNumber, byte[] pdfAttachment) {
        String subject = String.format("Facture %s - Facture Freelance", invoiceNumber);
        String body = buildInvoiceEmailBody(clientName, invoiceNumber);

        try {
            mailer.send(Mail.withHtml(to, subject, body)
                    .addAttachment(invoiceNumber + ".pdf", pdfAttachment, "application/pdf"));
            logger.infof("Invoice email sent to: %s", to);
        } catch (Exception e) {
            logger.errorf("Failed to send invoice email to %s: %s", to, e.getMessage());
            throw new RuntimeException("Failed to send invoice email", e);
        }
    }

    public void sendReminderEmail(String to, String clientName, String invoiceNumber, int daysOverdue) {
        String subject = String.format("Relance - Facture %s en retard", invoiceNumber);
        String body = buildReminderEmailBody(clientName, invoiceNumber, daysOverdue);

        try {
            mailer.send(Mail.withHtml(to, subject, body));
            logger.infof("Reminder email sent to: %s", to);
        } catch (Exception e) {
            logger.errorf("Failed to send reminder email to %s: %s", to, e.getMessage());
            throw new RuntimeException("Failed to send reminder email", e);
        }
    }

    private String buildOtpEmailBody(String otp, String firstName) {
        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2>Bonjour %s,</h2>
                    <p>Voici votre code de vérification pour vous connecter à Facture Freelance :</p>
                    <div style="background-color: #f0f0f0; padding: 20px; text-align: center; font-size: 32px; font-weight: bold; letter-spacing: 5px; margin: 20px 0;">
                        %s
                    </div>
                    <p>Ce code est valide pendant 5 minutes.</p>
                    <p>Si vous n'avez pas demandé ce code, veuillez ignorer cet email.</p>
                    <br>
                    <p>Cordialement,<br>L'équipe Facture Freelance</p>
                </body>
                </html>
                """, firstName, otp);
    }

    private String buildInvoiceEmailBody(String clientName, String invoiceNumber) {
        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2>Bonjour %s,</h2>
                    <p>Veuillez trouver ci-joint la facture <strong>%s</strong>.</p>
                    <p>Merci de procéder au règlement dans les délais convenus.</p>
                    <br>
                    <p>Cordialement</p>
                </body>
                </html>
                """, clientName, invoiceNumber);
    }

    private String buildReminderEmailBody(String clientName, String invoiceNumber, int daysOverdue) {
        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2>Bonjour %s,</h2>
                    <p>Nous constatons que la facture <strong>%s</strong> est en retard de <strong>%d jour(s)</strong>.</p>
                    <p>Merci de procéder au règlement dans les plus brefs délais.</p>
                    <p>Si vous avez déjà effectué le paiement, veuillez ignorer ce message.</p>
                    <br>
                    <p>Cordialement</p>
                </body>
                </html>
                """, clientName, invoiceNumber, daysOverdue);
    }
}
