package com.facture.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    private static final Logger logger = Logger.getLogger(GlobalExceptionHandler.class);

    @Override
    public Response toResponse(Exception exception) {
        logger.error("Exception occurred: " + exception.getMessage(), exception);

        if (exception instanceof AuthenticationException) {
            ErrorResponse error = new ErrorResponse(exception.getMessage(), 401);
            return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
        }

        if (exception instanceof jakarta.validation.ValidationException) {
            ErrorResponse error = new ErrorResponse(exception.getMessage(), 400);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        ErrorResponse error = new ErrorResponse("An internal server error occurred", 500);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }
}
