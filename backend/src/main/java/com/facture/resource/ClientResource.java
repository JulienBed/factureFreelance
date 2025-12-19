package com.facture.resource;

import com.facture.dto.ClientDto;
import com.facture.dto.CreateClientRequest;
import com.facture.service.ClientService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@Path("/api/clients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("User")
public class ClientResource {

    @Inject
    ClientService clientService;

    @Inject
    JsonWebToken jwt;

    @GET
    public Response getClients(@QueryParam("search") String search) {
        Long userId = Long.parseLong(jwt.getSubject());
        List<ClientDto> clients = clientService.getClientsByUserId(userId, search);
        return Response.ok(clients).build();
    }

    @GET
    @Path("/{id}")
    public Response getClient(@PathParam("id") Long id) {
        Long userId = Long.parseLong(jwt.getSubject());
        ClientDto client = clientService.getClientById(userId, id);
        return Response.ok(client).build();
    }

    @POST
    public Response createClient(@Valid CreateClientRequest request) {
        Long userId = Long.parseLong(jwt.getSubject());
        ClientDto client = clientService.createClient(userId, request);
        return Response.status(Response.Status.CREATED).entity(client).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateClient(@PathParam("id") Long id, @Valid CreateClientRequest request) {
        Long userId = Long.parseLong(jwt.getSubject());
        ClientDto client = clientService.updateClient(userId, id, request);
        return Response.ok(client).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteClient(@PathParam("id") Long id) {
        Long userId = Long.parseLong(jwt.getSubject());
        clientService.deleteClient(userId, id);
        return Response.noContent().build();
    }
}
