package com.revolut.modules;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;


public class PropertiesModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesModule.class);
    private static final String APPLICATION_PROPERTIES = "application.properties";


    @Override
    protected void configure() {
        Properties properties = new Properties();
        LOG.info("Loading {} ", APPLICATION_PROPERTIES);
        try (InputStream input = getPropertiesStream()) {
            properties.load(input);
            Names.bindProperties(binder(), properties);
        } catch (Exception ex) {
            LOG.error("Can't read file - {}, exception is {}. Suppressed exceptions - {}",
                    APPLICATION_PROPERTIES, ex, Arrays.toString(ex.getSuppressed()));
            throw new RuntimeException("Can't read properties", ex);
        }

    }

    private InputStream getPropertiesStream() {
        return this.getClass().getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES);
    }
}
