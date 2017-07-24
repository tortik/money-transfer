package com.revolut.modules;

import com.google.inject.AbstractModule;
import com.revolut.resources.FooResource;

public class ResourceModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(FooResource.class);
    }
}