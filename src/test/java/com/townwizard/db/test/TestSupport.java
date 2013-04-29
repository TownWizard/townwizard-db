package com.townwizard.db.test;

import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.townwizard.db.model.EntityInterceptor;
import com.townwizard.db.model.TwNamingStrategy;

/**
 * Superclass for all unit test classes
 * The purpose of this class is to load properties from test.properties file, and initialize
 * Hibernate session factory object
 */
public abstract class TestSupport {
    
    private static Properties properties;
    private static SessionFactory masterSessionFactory;
    private static SessionFactory directorySessionFactory;
    
    @BeforeClass
    public static void beforeTestsRun() throws Exception {
        properties = new Properties();
        properties.load(ClassLoader.getSystemResourceAsStream("test.properties"));
        masterSessionFactory = initSessionFactory("/hibernate.cfg.master.xml");
        directorySessionFactory = initSessionFactory("/hibernate.cfg.directory.xml");
    }
    
    @AfterClass
    public static void afterTestsRun() throws Exception {
        if(masterSessionFactory != null) {
            masterSessionFactory.close();
        }
        if(directorySessionFactory != null) {
            directorySessionFactory.close();
        }
    }
    
    protected static String getWebServicesUrlBase() {
        return properties.getProperty("web_services_url_base");
    }
    
    protected SessionFactory getMasterSessionFactory() {
        return masterSessionFactory;
    }
    
    protected SessionFactory getDirectorySessionFactory() {
        return directorySessionFactory;
    }

    private static SessionFactory initSessionFactory(String resource) {
        Configuration configuration = new Configuration();
        configuration.configure(resource);
        configuration.setNamingStrategy(new TwNamingStrategy());
        configuration.setInterceptor(new EntityInterceptor());
        ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().
                applySettings(configuration.getProperties()).buildServiceRegistry();
        return configuration.buildSessionFactory(serviceRegistry);
    }
}