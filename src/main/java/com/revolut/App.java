package com.revolut;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.revolut.modules.*;
import com.revolut.servlet.swagger.SwaggerServletContextListener;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.google.inject.servlet.GuiceFilter;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Slf4jLog;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

public class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);
    public static final String APPLICATION_PATH = "/api";
    public static final String CONTEXT_ROOT = "/";

    private final GuiceFilter filter;
    private final int port;

    @Inject
    public App(GuiceFilter filter, @Named("application.port") String port) {
        this.filter = filter;
        this.port = Integer.valueOf(port);
    }


    public static void main(String[] args) throws Exception {
            Log.setLog(new Slf4jLog());

            final Injector injector = Guice.createInjector(new JettyModule(),
                    new RestEasyModule(APPLICATION_PATH),
                    new ResourceModule(),
                    new SwaggerModule(APPLICATION_PATH),
                    new PropertiesModule(),
                    new AppModule());

            injector.getInstance(App.class).startServer(injector);
    }

    public void startServer(Injector injector) throws Exception {

        Server server = new Server(port);

        final ServletContextHandler context = new ServletContextHandler(server, CONTEXT_ROOT);

        FilterHolder filterHolder = new FilterHolder(filter);
        context.addFilter(filterHolder, APPLICATION_PATH + "/*", null);
        context.addFilter(GuiceFilter.class, "/*", null);
        context.addServlet(DefaultServlet.class, CONTEXT_ROOT);

        String resourceBasePath = App.class.getResource("/swagger-ui").toExternalForm();
        context.setResourceBase(resourceBasePath);
        context.setWelcomeFiles(new String[]{"index.html"});

        context.addEventListener(injector.getInstance(SwaggerServletContextListener.class));
        context.addEventListener(injector.getInstance(GuiceResteasyBootstrapServletContextListener.class));

        addShutdownHook(server);

        server.start();
        LOG.info("Successfully start server on port - {}", port);
        server.join();
    }

    private void addShutdownHook(Server server) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Exiting");
            try {
                server.stop();
            } catch (Exception e) {
                LOG.error("Exception on server stop", e);
            }
        }));
    }


}
