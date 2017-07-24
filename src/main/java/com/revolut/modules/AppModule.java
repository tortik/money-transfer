package com.revolut.modules;

import com.google.inject.AbstractModule;
import com.revolut.core.FooManager;
import com.revolut.core.FooManagerImpl;

public class AppModule extends AbstractModule {
	@Override
	public void configure() {
		bind(FooManager.class).to(FooManagerImpl.class).asEagerSingleton();
	}
}
