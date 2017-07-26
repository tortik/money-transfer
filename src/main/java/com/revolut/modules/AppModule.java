package com.revolut.modules;

import com.google.inject.AbstractModule;
import com.revolut.core.dao.AccBalanceMapRepository;
import com.revolut.core.dao.AccountBalanceRepository;
import com.revolut.core.service.MoneyTransfer;
import com.revolut.core.service.lock.LockHolder;
import com.revolut.core.service.lock.MoneyTransferLockService;
import com.revolut.core.service.sync.SyncMonitorHolder;

public class AppModule extends AbstractModule {
	@Override
	public void configure() {
		bind(SyncMonitorHolder.class);
		bind(LockHolder.class);
		bind(AccountBalanceRepository.class).to(AccBalanceMapRepository.class).asEagerSingleton();
		bind(MoneyTransfer.class).to(MoneyTransferLockService.class).asEagerSingleton();
	}
}
