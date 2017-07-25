package com.revolut.resources;

import com.google.inject.Inject;
import com.revolut.core.model.AccountBalance;
import com.revolut.core.service.MoneyTransfer;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("accounts")
public class AccountBalanceResource {

    private final MoneyTransfer service;

    @Inject
    public AccountBalanceResource(MoneyTransfer service) {
        this.service = service;
    }


    @GET
    @Produces("application/json")
    @Path("{acccountId}/balance")
    public AccountBalance retrieve(@ApiParam @PathParam("acccountId") Long accountId) {
        return service.getBalance(accountId);
    }
}
