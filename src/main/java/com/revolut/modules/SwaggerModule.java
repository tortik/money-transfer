package com.revolut.modules;

import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletModule;
import com.revolut.servlet.swagger.ApiOriginFilter;
import com.revolut.servlet.swagger.SwaggerServletContextListener;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

import javax.servlet.ServletContextListener;

public class SwaggerModule extends ServletModule {

    private final String path;

    public SwaggerModule() {
        this.path = null;
    }

    public SwaggerModule(final String path) {
        this.path = path;
    }

    @Override
    protected void configureServlets() {

        Multibinder<ServletContextListener> multibinder = Multibinder.newSetBinder(binder(),
                ServletContextListener.class);
        multibinder.addBinding().to(SwaggerServletContextListener.class);

        bind(ApiListingResource.class);
        bind(SwaggerSerializers.class);

        if (path == null) {
            filter("/*").through(ApiOriginFilter.class);
        } else {
            filter(path + "/*").through(ApiOriginFilter.class);
        }
    }
}