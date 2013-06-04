package com.townwizard.db.application;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.townwizard.db.logger.Log;
import com.townwizard.globaldata.ingest.place.IngestHttpExecutors;
import com.townwizard.globaldata.ingest.place.IngestQueue;
import com.townwizard.globaldata.ingest.place.Ingesters;

public class ContextLoaderListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {}

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        deregisterJdbcDriver();        
        
        Ingesters.shutdownThreads();
        IngestHttpExecutors.shutdownThreads();
        IngestQueue.shutdownThreads();
    }
    
    // This manually deregisters JDBC driver, which prevents Tomcat 7 from complaining about memory leaks
    private void deregisterJdbcDriver() {
       Enumeration<Driver> drivers = DriverManager.getDrivers();  
       while (drivers.hasMoreElements()) {  
          Driver driver = drivers.nextElement();  
          try  {  
             DriverManager.deregisterDriver(driver);  
             Log.info(String.format("Deregistering jdbc driver: %s", driver));  
          } catch (SQLException e) {  
             Log.info(String.format("Error deregistering driver %s", driver));
             Log.exception(e);
          }  
       }
    }
    
}
