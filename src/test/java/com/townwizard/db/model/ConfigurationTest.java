package com.townwizard.db.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.townwizard.db.configuration.ConfigurationAttribute;
import com.townwizard.db.test.TestSupport;

public class ConfigurationTest extends TestSupport {

    private Session session;
    
    @Before
    public void beginTransaction() {
        session = getMasterSessionFactory().openSession();        
        session.beginTransaction();        
    }
    
    @After
    public void rollbackTransaction() {
        try {
            if(session != null) {
                session.getTransaction().rollback();                
            }
        } finally {
            if(session != null) {
                session.close();
            }
        }
    }
    
    @Test
    public void testConfiguration() {
        ConfigurationAttribute a = new ConfigurationAttribute("TEST_KEY", "TEST_VALUE", "TEST_DESCRIPTION");
        session.save(a);
        session.flush();
        
        Integer id = a.getId();
        assertNotNull("Configuration attribute id should not be null after save()", id);
        
        ConfigurationAttribute fromDb = 
                (ConfigurationAttribute)session.get(ConfigurationAttribute.class, id);
        assertNotNull("Configuration should be found in db after save()", fromDb);
        
        session.delete(fromDb);
        fromDb = (ConfigurationAttribute)session.get(ConfigurationAttribute.class, id);
        assertNull("Configuration should not be found in db after delete()", fromDb);
    }

}
