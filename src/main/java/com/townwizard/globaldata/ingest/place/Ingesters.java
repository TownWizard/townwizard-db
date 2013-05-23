package com.townwizard.globaldata.ingest.place;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import net.sf.ehcache.util.NamedThreadFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.logger.Log;
import com.townwizard.globaldata.model.directory.PlaceCategory;

@Component("placeIngesters")
public final class Ingesters implements Runnable {

    private static final String THREAD_NAME_PREFIX = "ingester"; 

    private static ExecutorService ingestersLoop;
    
    private Map<String, Ingester> ingesters = new HashMap<>();

    @Autowired
    private IngestQueue placeIngestQueue;

    @PostConstruct
    public void init() {
        ingestersLoop = Executors.newFixedThreadPool(1, new NamedThreadFactory(THREAD_NAME_PREFIX));
        ingestersLoop.submit(this);
        Log.info("Place ingesters loop started");
    }

    public void startIngest(String zipCode, String countryCode, List<PlaceCategory> categories) {
        String key = countryCode + "_" + zipCode;
        if(!ingesters.containsKey(key)) {
            ingesters.put(key, IngesterFactory.getIngester(zipCode, countryCode, categories));
        }
    }
    
    @Override
    public void run() {
        while(true) {
            try {
                IngestTask task = placeIngestQueue.dequeue();
                if(task != null) {
                    String key = task.getCountryCode() + "_" + task.getZipCode();
                    Ingester ingester = ingesters.get(key);
                    if(ingester != null) {
                        ingester.addProcessedIngestTaskResult(task);
                    }
                } else {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        Log.info("Exiting place ingesters loop...");
                        return;
                    }
                }
            } catch (Exception e) {
                Log.exception(e);
            }
        }
    }
    
}
