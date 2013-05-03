package com.townwizard.globaldata.service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.logger.Log;
import com.townwizard.globaldata.model.directory.Ingest;
import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.model.directory.PlaceIngest;
import com.townwizard.globaldata.model.directory.ZipIngest;
import com.townwizard.globaldata.service.provider.YellowPagesService;

@Component("placeIngester")
public final class PlaceIngester {
    
    @Autowired
    private YellowPagesService yellowPagesService;
    @Autowired
    private PlaceService placeService;
    
    @PostConstruct
    public void init() {
        dbExecutor.submit(new DbExecutor());
        queueMonitor.submit(new IngestQueueMonitor());
    }
    
    //This executor is responsible for saving ingests in the DB.
    private static final ExecutorService dbExecutor = Executors.newFixedThreadPool(1);
    
    //This executor is responsible for monitoring the queue.
    private static final ExecutorService queueMonitor = Executors.newFixedThreadPool(1);    
    
    //These executors will bring places from the source in parallel.
    private static final ExecutorService httpExecutors = Executors.newFixedThreadPool(10);
    
    //The http executors will be placing category ingests in this queue, and the 
    //db thread will be taking ingest from it and save ingests in the DB
    private static final BlockingQueue<IngestItem> ingestQueue = new LinkedBlockingQueue<>();
    
    private final static class IngestItem {
        String zip, countryCode, category;
        int distanceInMeters;
        List<Place> places;
        int countDown;
        
        IngestItem(String zip, String countryCode, int distanceInMeters, String category,
                List<Place> places, int countDown) {
            this.zip = zip;
            this.countryCode = countryCode;
            this.distanceInMeters = distanceInMeters;
            this.category = category;
            this.places = places;
            this.countDown = countDown;
        }
    }
    
    private final class HttpExecutor implements Runnable {
        
        private String zip, countryCode, category;        
        private int distanceInMeters;
        private int countDown;
        
        HttpExecutor(String zip, String countryCode, int distanceInMeters, String category, int countDown) {
            this.zip = zip;
            this.countryCode = countryCode;
            this.distanceInMeters = distanceInMeters;
            this.category = category;
            this.countDown = countDown;
        }

        @Override
        public void run() {
            try {
                List<Place> places = getPlacesFromSource(zip, countryCode, distanceInMeters, category);
                //Log.debug(category + ": " + places.size());
                ingestQueue.put(new IngestItem(zip, countryCode, distanceInMeters, category, places, countDown));                
            } catch(Exception e) {
                Log.exception(e);
                e.printStackTrace();
            }
        }
    }
    
    private final class DbExecutor implements Runnable {
        
        DbExecutor() {
            if(Log.isInfoEnabled()) {
                Log.info("Place ingest DB executor started.");
            }
        }
        
        @Override
        public void run() {
            while(true) {
                try {
                    IngestItem item = ingestQueue.take();                    
                    doIngestByZipAndCategory(
                            item.zip, item.countryCode, item.distanceInMeters, item.category, item.places);
                    if(item.countDown == 0) {
                        ZipIngest ingest = placeService.getZipIngest(item.zip, item.countryCode);
                        if(ingest != null) {
                            if(Log.isInfoEnabled()) Log.info("Finishing ingest for zip: " + item.zip);
                            ingest.setStatus(Ingest.Status.R);
                            ingest.setFinished(new Date());
                            placeService.updateZipIngest(ingest);
                        }
                    }
                } catch (Exception e) {
                    Log.exception(e);
                    e.printStackTrace();
                }
            }                
        }        
    }
    
    private final class IngestQueueMonitor implements Runnable {
        
        IngestQueueMonitor() {
            if(Log.isInfoEnabled()) {
                Log.info("Place ingest queue monitor started.");
            }
        }
        
        @Override
        public void run() {
            while(true) {
                try {
                    int queueSize = ingestQueue.size();
                    if(queueSize > 0) {
                        if(Log.isInfoEnabled()) {
                            Log.info("Ingest queue size: " + queueSize);
                        }
                    }
                    Thread.sleep(10000);
                } catch (Exception e) {
                    Log.exception(e);
                }
            }                
        }        
    }
    
    public void ingestByZip(String zipCode, String countryCode, int distanceInMeters) {
        ZipIngest ingest = placeService.getZipIngest(zipCode, countryCode);
        if(ingest.getStatus() != ZipIngest.Status.N) return;
        
        if(Log.isInfoEnabled()) Log.info("Starting ingest for zip: " + zipCode);
        
        ingest.setStatus(Ingest.Status.I);
        placeService.updateZipIngest(ingest);
        
        List<String> categories = placeService.getAllPlaceCategoryNames();
        int countDown = categories.size();
        for(String category : categories) {
            if(--countDown == 0) {
                if(Log.isDebugEnabled()) Log.debug("Submitting zero item for zip: " +  zipCode);
            }
            httpExecutors.submit(new HttpExecutor(zipCode, countryCode, distanceInMeters, category, countDown));
        }
        
    }
    
    public Object[] ingestByZipAndCategory(
            String zipCode, String countryCode, int distanceInMeters, String categoryOrTerm) {
        return doIngestByZipAndCategory(zipCode, countryCode, distanceInMeters, categoryOrTerm, null);
    } 
    
    private Object[] doIngestByZipAndCategory(String zipCode, String countryCode,
            int distanceInMeters, String categoryOrTerm, List<Place> placeList) {
        
        PlaceIngest ingest = placeService.getIngest(
                zipCode, countryCode, distanceInMeters, categoryOrTerm);
        PlaceIngest.Status status = ingest.getStatus();
        
        List<Place> places = placeList;
        if(status != PlaceIngest.Status.R) {
            if(places == null) {
                places = getPlacesFromSource(zipCode, countryCode, distanceInMeters, categoryOrTerm);
            }
            if(status == PlaceIngest.Status.N) {
                placeService.saveIngest(ingest, places);
                /*
                if(ingest.getPlaceCategory() != null) {
                    if(Log.isDebugEnabled()) {
                        Log.debug("Ingested " + places.size() + " places for zip '" + zipCode + 
                                "' and category '" + categoryOrTerm + "'");
                    }
                }
                */                
            }
        }
        
        return new Object[]{ingest, places};        
    }
    
    private List<Place> getPlacesFromSource(
            String zipCode, String countryCode, int distanceInMeters, String categoryOrTerm) {
        List<Place> places = yellowPagesService.getPlaces(zipCode, distanceInMeters, categoryOrTerm);
        for(Place p : places) p.setCountryCode(countryCode);
        return places;
    }

}
