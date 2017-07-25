package com.revolut.modules;

import com.google.inject.AbstractModule;
import com.revolut.core.service.MoneyTransfer;
import com.revolut.core.service.sync.MoneyTransferSyncService;

public class AppModule extends AbstractModule {
	@Override
	public void configure() {
		bind(MoneyTransfer.class).to(MoneyTransferSyncService.class).asEagerSingleton();
	}
}
