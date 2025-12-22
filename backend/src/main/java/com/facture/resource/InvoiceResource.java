package com.facture.resource;

import com.facture.dto.CreateInvoiceRequest;
import com.facture.dto.InvoiceDto;
import com.facture.entity.Invoice;
import com.facture.entity.InvoiceStatus;
import com.facture.service.InvoiceService;
import com.facture.service.PdfService;
import com.facture.service.OpenSearchService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/invoices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("User")
public class InvoiceResource {

    @Inject
    Logger logger;

    @Inject
    InvoiceService invoiceService;

    @Inject
    PdfService pdfService;

    @Inject
    OpenSearchService openSearchService;

    @Inject
    JsonWebToken jwt;

    @GET
    public Response getInvoices(@QueryParam("status") InvoiceStatus status) {
        Long userId = Long.parseLong(jwt.getSubject());
        List<InvoiceDto> invoices = invoiceService.getInvoicesByUserId(userId, status);
        return Response.ok(invoices).build();
    }

    @GET
    @Path("/{id}")
    public Response getInvoice(@PathParam("id") Long id) {
        Long userId = Long.parseLong(jwt.getSubject());
        InvoiceDto invoice = invoiceService.getInvoiceById(userId, id);
        return Response.ok(invoice).build();
    }

    @POST
    public Response createInvoice(@Valid CreateInvoiceRequest request) {
        Long userId = Long.parseLong(jwt.getSubject());
        InvoiceDto invoice = invoiceService.createInvoice(userId, request);
        return Response.status(Response.Status.CREATED).entity(invoice).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateInvoice(@PathParam("id") Long id, @Valid CreateInvoiceRequest request) {
        Long userId = Long.parseLong(jwt.getSubject());
        InvoiceDto invoice = invoiceService.updateInvoice(userId, id, request);
        return Response.ok(invoice).build();
    }

    @PUT
    @Path("/{id}/status")
    public Response updateInvoiceStatus(@PathParam("id") Long id, @QueryParam("status") InvoiceStatus status) {
        Long userId = Long.parseLong(jwt.getSubject());
        InvoiceDto invoice = invoiceService.updateInvoiceStatus(userId, id, status);
        return Response.ok(invoice).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteInvoice(@PathParam("id") Long id) {
        Long userId = Long.parseLong(jwt.getSubject());
        invoiceService.deleteInvoice(userId, id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/pdf")
    @Produces("application/pdf")
    public Response downloadInvoicePdf(@PathParam("id") Long id) {
        Long userId = Long.parseLong(jwt.getSubject());

        try {
            // Get the invoice entity (not DTO) with all relations
            Invoice invoice = Invoice.findById(id);
            if (invoice == null || !invoice.user.id.equals(userId)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"Invoice not found\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }

            // Generate Factur-X PDF
            byte[] pdfBytes = pdfService.generateFacturXPdf(invoice);

            // Return PDF with proper headers
            String filename = invoice.invoiceNumber + "_Factur-X.pdf";
            return Response.ok(pdfBytes)
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .header("Content-Type", "application/pdf")
                    .build();

        } catch (Exception e) {
            logger.errorf("Error generating PDF for invoice %d: %s", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error generating PDF: " + e.getMessage() + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @POST
    @Path("/{id}/send")
    public Response sendInvoiceByEmail(@PathParam("id") Long id) {
        Long userId = Long.parseLong(jwt.getSubject());

        try {
            Invoice invoice = Invoice.findById(id);
            if (invoice == null || !invoice.user.id.equals(userId)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"Invoice not found\"}")
                        .build();
            }

            // Generate PDF
            byte[] pdfBytes = pdfService.generateFacturXPdf(invoice);

            // TODO: Send email with PDF attachment
            // emailService.sendInvoiceEmail(invoice.client.email, invoice.client.companyName, invoice.invoiceNumber, pdfBytes);

            return Response.ok()
                    .entity("{\"message\": \"Invoice sent successfully\"}")
                    .build();

        } catch (Exception e) {
            logger.errorf("Error sending invoice %d: %s", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error sending invoice: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/search")
    public Response searchInvoices(@QueryParam("q") String query) {
        Long userId = Long.parseLong(jwt.getSubject());

        try {
            if (query == null || query.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Query parameter 'q' is required\"}")
                        .build();
            }

            // Search in OpenSearch
            List<Long> invoiceIds = openSearchService.searchInvoices(userId, query);

            // Fetch invoice DTOs for the found IDs
            List<InvoiceDto> invoices = invoiceIds.stream()
                    .map(id -> {
                        try {
                            return invoiceService.getInvoiceById(userId, id);
                        } catch (Exception e) {
                            logger.warnf("Invoice %d found in search but not accessible: %s", id, e.getMessage());
                            return null;
                        }
                    })
                    .filter(inv -> inv != null)
                    .collect(Collectors.toList());

            logger.infof("Search for '%s' returned %d results for user %d", query, invoices.size(), userId);
            return Response.ok(invoices).build();

        } catch (Exception e) {
            logger.errorf("Error searching invoices: %s", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error searching invoices: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
