package com.revolut.resources;

import com.google.inject.Inject;
import com.revolut.core.model.AccountBalance;
import com.revolut.core.service.MoneyTransfer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api
@Path("accounts")
public class AccountBalanceResource {

    private final MoneyTransfer service;

    @Inject
    public AccountBalanceResource(MoneyTransfer service) {
        this.service = service;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{acccountId}/balance")
    public AccountBalance retrieve(@ApiParam @PathParam("acccountId") Long accountId) {
        return service.getBalance(accountId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addAccount(AccountBalance balance) {
         service.addBalance(balance);
        return Response.status(204).build();
    }
}
