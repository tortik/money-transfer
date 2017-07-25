package com.revolut.modules;

import com.google.inject.AbstractModule;
import com.revolut.resources.AccountBalanceResource;
import com.revolut.resources.MoneyTransferResource;

public class ResourceModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(MoneyTransferResource.class);
        bind(AccountBalanceResource.class);
    }
}