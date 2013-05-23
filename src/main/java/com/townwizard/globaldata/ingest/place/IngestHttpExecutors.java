package com.townwizard.globaldata.ingest.place;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
public class IngestHttpExecutors implements ConfigurationListener {
    
    private static final String THREAD_NAME_PREFIX = "http-executor"; 
    
    @Autowired private ConfigurationService configurationService;
    @Autowired private IngestQueue placeIngestQueue;
    @Autowired private YellowPagesService yellowPagesService;

    private static ExecutorService httpExecutors;
    private static boolean stoppedFlag = false;
    private static boolean shutdownFlag = false;
    
    public void submit(final String zipCode, final String countryCode, final String category) {
        httpExecutors.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        if(stoppedFlag || shutdownFlag) return;
                        try {
                            List<Place> places = getPlacesFromSource(zipCode, countryCode, category);
                            placeIngestQueue.enqueue(new IngestTask(zipCode, countryCode, category, places));
                        } catch(Exception e) {
                            Log.exception(e);
                        }
                    }
                });
    }
    
    @PostConstruct
    public void init() {
        httpExecutors = Executors.newFixedThreadPool(
                configurationService.getIntValue(ConfigurationKey.PLACE_INGEST_NUM_HTTP_EXECUTORS),
                new NamedThreadFactory(THREAD_NAME_PREFIX));        
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
                List<Runnable> awaitingTasks = httpExecutors.shutdownNow();                
                httpExecutors = Executors.newFixedThreadPool(
                        configurationService.getIntValue(key),
                        new NamedThreadFactory(THREAD_NAME_PREFIX));
                for(Runnable task : awaitingTasks) {
                    httpExecutors.submit(task);
                }
            }
        } else if(key == ConfigurationKey.PLACE_INGEST_STOPPED) {
            stoppedFlag = true;
        }
    }
    
    private List<Place> getPlacesFromSource(String zipCode, String countryCode, String category) {
        List<Place> places = yellowPagesService.getPlaces(zipCode, category);
        for(Place p : places) p.setCountryCode(countryCode);
        return places;
    }

}
