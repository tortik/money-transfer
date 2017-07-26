package com.revolut.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.revolut.core.model.TransferRequest;
import com.revolut.core.service.MoneyTransfer;

import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;


@Api
@Path("/transfers")
public class MoneyTransferResource {
    private static final Logger LOG = LoggerFactory.getLogger(MoneyTransferResource.class);


    private final MoneyTransfer service;

    @Inject
    public MoneyTransferResource(MoneyTransfer service) {
        this.service = service;
    }

    @POST
    @Consumes("application/json")
    public Response transfer(@ApiParam TransferRequest request) {
        if (!validateParams(request)) {
            return Response.status(400).build();
        }
        service.transfer(request);

        return Response.status(204).build();
    }

    private boolean validateParams(TransferRequest request) {
        boolean invalid;
        if (invalid = request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            LOG.warn("VALIDATION_ERROR: Can't transfer amount {} less or equal to zero", request.getAmount());
        }
        if (invalid = !invalid && request.getFromAcc() == request.getToAcc()) {
            LOG.warn("VALIDATION_ERROR: Can't send money to the same account {} = {}", request.getFromAcc(), request.getToAcc());
        }
        if (invalid = !invalid && service.getBalance(request.getFromAcc()) == null) {
            LOG.warn("VALIDATION_ERROR: Can't find sender account {}", request.getFromAcc());
        }
        if (invalid = !invalid && service.getBalance(request.getToAcc()) == null) {
            LOG.warn("VALIDATION_ERROR: Can't find recipient account {}", request.getToAcc());
        }
        return !invalid;
    }
}
