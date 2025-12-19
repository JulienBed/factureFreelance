package com.facture.resource;

import com.facture.dto.*;
import com.facture.service.AuthService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    Logger logger;

    @Inject
    AuthService authService;

    @POST
    @Path("/register")
    public Response register(@Valid RegisterRequest request) {
        logger.infof("Registration attempt for email: %s", request.email);
        authService.register(request);
        return Response.status(Response.Status.CREATED)
                .entity(new MessageResponse("User registered successfully. Please login to receive OTP."))
                .build();
    }

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        logger.infof("Login attempt for email: %s", request.email);
        authService.login(request);
        return Response.ok(new MessageResponse("OTP sent to your email. Please verify to complete login.")).build();
    }

    @POST
    @Path("/verify-otp")
    public Response verifyOtp(@Valid VerifyOtpRequest request) {
        logger.infof("OTP verification attempt for email: %s", request.email);
        AuthResponse response = authService.verifyOtp(request);
        return Response.ok(response).build();
    }

    // Simple message response class
    public static class MessageResponse {
        public String message;

        public MessageResponse() {
        }

        public MessageResponse(String message) {
            this.message = message;
        }
    }
}
