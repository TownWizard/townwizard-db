package com.townwizard.globaldata.ingest.place;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.sf.ehcache.util.NamedThreadFactory;

import com.townwizard.db.configuration.ConfigurationKey;
import com.townwizard.db.configuration.ConfigurationListener;
import com.townwizard.db.configuration.ConfigurationService;
import com.townwizard.db.logger.Log;
import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.service.provider.YellowPagesService;

@Component("placeIngestHttpExecutors")
public class IngestHttpExecutors implements Runnable, ConfigurationListener {
    
    private static final String THREAD_NAME_PREFIX = "http-executor"; 
    
    @Autowired private ConfigurationService configurationService;
    @Autowired private IngestQueue placeIngestQueue;
    @Autowired private YellowPagesService yellowPagesService;

    private static ExecutorService httpExecutors;
    private static boolean stoppedFlag = false;
    private static boolean shutdownFlag = false;
    
    @Override
    public void run() {        
        while (true) {
            if(stoppedFlag || shutdownFlag) return;
            if(Thread.interrupted()) return;
            
            IngestTask task = null;
            try {
                task = placeIngestQueue.getHighPriorityHttpTask();
                if(task == null) {
                    task = placeIngestQueue.getHttpTask();
                }
                if(task != null) {
                    List<Place> places = getPlacesFromSource(
                            task.getZipCode(), task.getCountryCode(), task.getCategory());
                    placeIngestQueue.addDbTask(
                            new IngestTask(task.getZipCode(), task.getCountryCode(), task.getCategory(),
                                    task.isHighPriority(), places));                    
                } else {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        Log.info("Exiting http executor " + Thread.currentThread().getName());
                        return;
                    }
                }
            } catch (Exception e) {
                if(task != null) {
                    placeIngestQueue.addHttpTask(task);
                }
                if(e instanceof InterruptedException) {
                    Log.info("Exiting http executor " + Thread.currentThread().getName());
                    return;
                }
                Log.exception(e);
            }
        }
    }
    
    @PostConstruct
    public void init() {
        int numExecutors = configurationService.getIntValue(ConfigurationKey.PLACE_INGEST_NUM_HTTP_EXECUTORS); 
        httpExecutors = Executors.newFixedThreadPool(numExecutors, new NamedThreadFactory(THREAD_NAME_PREFIX));
        for(int i = 0; i < numExecutors; i++) httpExecutors.submit(this);
        configurationService.addConfigurationListener(this);
        Log.info("Place ingest http executors service started");
    }
    
    private final ConfigurationKey[] keysOfInterest = {
            ConfigurationKey.PLACE_INGEST_NUM_HTTP_EXECUTORS,
            ConfigurationKey.PLACE_INGEST_STOPPED
    };
    
    @Override
    public ConfigurationKey[] keysOfInterest() {
        return keysOfInterest;        
    }

    @Override
    public void configurationChanged(ConfigurationKey key) {
        if(key == ConfigurationKey.PLACE_INGEST_NUM_HTTP_EXECUTORS) {
            synchronized (httpExecutors) {
                httpExecutors.shutdownNow();
                try {
                    if(!httpExecutors.awaitTermination(120, TimeUnit.SECONDS)) {
                        Log.warning("////////////////// Cannot terminate http executors...");
                        return;
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                int numExecutors = configurationService.getIntValue(key); 
                httpExecutors = Executors.newFixedThreadPool(
                        numExecutors, new NamedThreadFactory(THREAD_NAME_PREFIX));
                for(int i = 0; i < numExecutors; i++) httpExecutors.submit(this);
                Log.info("Place ingest http executors pool size changed to: " + numExecutors);
            }
        } else if(key == ConfigurationKey.PLACE_INGEST_STOPPED) {
            stoppedFlag = configurationService.getBooleanValue(ConfigurationKey.PLACE_INGEST_STOPPED);
        }
    }
    
    public static void shutdownThreads() {
        if(httpExecutors != null) {
            Log.info("About to shutdown place ingest http executors...");
            httpExecutors.shutdownNow();
            
            try {
                if(!httpExecutors.awaitTermination(30, TimeUnit.SECONDS)) {
                    Log.warning("Cannot terminate http executors...");
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private List<Place> getPlacesFromSource(String zipCode, String countryCode, String category) 
            throws Exception {
        List<Place> places = yellowPagesService.getPlaces(zipCode, category);
        for(Place p : places) p.setCountryCode(countryCode);
        return places;
    }

}
