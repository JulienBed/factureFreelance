package com.facture.resource;

import com.facture.dto.CreateInvoiceRequest;
import com.facture.dto.InvoiceDto;
import com.facture.entity.InvoiceStatus;
import com.facture.service.InvoiceService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@Path("/api/invoices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("User")
public class InvoiceResource {

    @Inject
    InvoiceService invoiceService;

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
}
