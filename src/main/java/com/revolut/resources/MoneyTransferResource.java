package com.revolut.resources;

import javax.ws.rs.*;

import com.revolut.core.service.MoneyTransfer;

import com.google.inject.Inject;

@Path("/transfers")
public class MoneyTransferResource {
	private final MoneyTransfer manager;

	@Inject
	public MoneyTransferResource(MoneyTransfer manager) {
		this.manager = manager;
	}

	@GET
	@Produces("application/json")
	public String retrieve() {
		return manager.getFoo();
	}

	@POST
	@Consumes("application/json")
	public void update(String foo) {
		manager.setFoo(foo);
	}
}
