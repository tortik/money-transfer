package com.revolut.servlet.swagger;

import com.revolut.resources.FooResource;
import io.swagger.config.ScannerFactory;
import io.swagger.jaxrs.config.BeanConfig;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

 public class SwaggerServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {

        BeanConfig beanConfig = getBeanConfig();
        event.getServletContext().setAttribute("reader", beanConfig);
        event.getServletContext().setAttribute("swagger", beanConfig.getSwagger());
        event.getServletContext().setAttribute("scanner", ScannerFactory.getScanner());
    }

    private BeanConfig getBeanConfig() {

        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setSchemes(new String[] { "http" });
        beanConfig.setHost("localhost:8080");
        beanConfig.setBasePath("/api");

        beanConfig.setTitle("RESTEasy, Embedded Jetty, Swagger and Guice");
        beanConfig.setDescription("Money transfer application.");


        beanConfig.setResourcePackage(FooResource.class.getPackage().getName());
        beanConfig.setScan(true);

        return beanConfig;
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }
}
