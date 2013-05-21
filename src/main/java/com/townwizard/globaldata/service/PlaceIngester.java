package com.townwizard.globaldata.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import net.sf.ehcache.util.NamedThreadFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.configuration.ConfigurationKey;
import com.townwizard.db.configuration.ConfigurationListener;
import com.townwizard.db.configuration.ConfigurationService;
import com.townwizard.db.logger.Log;
import com.townwizard.globaldata.model.directory.Ingest;
import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.model.directory.PlaceIngest;
import com.townwizard.globaldata.model.directory.ZipIngest;
import com.townwizard.globaldata.service.provider.YellowPagesService;

@Component("placeIngester")
public final class PlaceIngester implements ConfigurationListener {
    
    @Autowired
    private YellowPagesService yellowPagesService;
    @Autowired
    private PlaceService placeService;
    @Autowired
    private ConfigurationService configurationService;
    
    @PostConstruct
    public void init() {         
        dbExecutor = Executors.newFixedThreadPool(1, new NamedThreadFactory("db-executor"));
        queueMonitor = Executors.newFixedThreadPool(1, new NamedThreadFactory("queue-monitor"));
        
        httpExecutors = Executors.newFixedThreadPool(
                configurationService.getIntValue(ConfigurationKey.PLACE_INGEST_NUM_HTTP_EXECUTORS),
                new NamedThreadFactory("http-executor"));
        
        dbExecutor.submit(new DbExecutor());
        queueMonitor.submit(new IngestQueueMonitor());
        
        configurationService.addConfigurationListener(this);
    }
    
    private static boolean shutdownFlag = false;
    private static boolean stoppedFlag = false;
    
    //This executor is responsible for saving ingests in the DB.
    private static ExecutorService dbExecutor;
    
    //This executor is responsible for monitoring the queue.
    private static ExecutorService queueMonitor;    
    
    //These executors will bring places from the source in parallel.
    private static ExecutorService httpExecutors;
    
    //The http executors will be placing category ingests in this queue, and the 
    //db thread will be taking ingest from it and save ingests in the DB
    //private static final BlockingQueue<IngestItem> ingestQueue = new LinkedBlockingQueue<>();
    private static final Queue<IngestItem> ingestQueue = new ConcurrentLinkedQueue<>();
        
    private final static class IngestItem {
        String zip, countryCode, category;
        List<Place> places;
        int countDown;        
        
        IngestItem(String zip, String countryCode, String category, List<Place> places, int countDown) {
            this.zip = zip;
            this.countryCode = countryCode;            
            this.category = category;
            this.places = places;
            this.countDown = countDown;
        }
    }
    
    private final class HttpExecutor implements Runnable {
        
        private String zip, countryCode, category;
        private int countDown;
        
        HttpExecutor(String zip, String countryCode, String category, int countDown) {
            this.zip = zip;
            this.countryCode = countryCode;            
            this.category = category;
            this.countDown = countDown;
        }

        @Override
        public void run() {
            if(stoppedFlag || shutdownFlag) return;
            try {
                List<Place> places = null;
                try {
                    places = getPlacesFromSource(zip, countryCode, category, null);
                } catch(Exception e) {
                    places = Collections.emptyList();
                    Log.exception(e);
                    e.printStackTrace();
                }
                ingestQueue.add(new IngestItem(zip, countryCode, category, places, countDown));
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
                if(shutdownFlag) {
                    Log.info("Exiting place ingest DB executor...");
                    return;
                }
                try {
                    IngestItem item = ingestQueue.poll();
                    if(item != null) {
                        getPlacesAndOptionallyIngest(item.zip, item.countryCode, item.category, null, item.places);
                        if(item.countDown == 0) {
                            ZipIngest ingest = placeService.getZipIngest(item.zip, item.countryCode);
                            if(ingest != null) {
                                if(Log.isInfoEnabled()) Log.info("Finishing ingest for zip: " + item.zip);
                                ingest.setStatus(Ingest.Status.R);
                                ingest.setFinished(new Date());
                                placeService.updateZipIngest(ingest);
                            }
                        }
                    } else {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ie) {
                            Log.info("Exiting place ingest DB executor...");
                            return;
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
                if(shutdownFlag) {
                    Log.info("Exiting place ingest queue monitor...");
                    return;
                }
                try {
                    int queueSize = ingestQueue.size();
                    if(queueSize > 0) {
                        if(Log.isInfoEnabled()) {
                            Log.info("Ingest queue size: " + queueSize);
                        }
                    }
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ie) {
                        Log.info("Exiting place ingest queue monitor...");
                        return;
                    }
                } catch (Exception e) {
                    Log.exception(e);
                }
            }                
        }        
    }
    
    public static void shutdownIngestThreads() {
        shutdownFlag = true;
        
        Log.info("About to shutdown executors...");        
        if(PlaceIngester.httpExecutors != null) PlaceIngester.httpExecutors.shutdownNow();
        if(PlaceIngester.queueMonitor != null) PlaceIngester.queueMonitor.shutdownNow();        
        if(PlaceIngester.dbExecutor != null) PlaceIngester.dbExecutor.shutdownNow();
        
        int attempt = 1;
        while(!((httpExecutors == null || httpExecutors.isTerminated()) &&
                (dbExecutor == null || dbExecutor.isTerminated()) &&
                (queueMonitor == null || queueMonitor.isTerminated()))) {
            if(attempt++ > 5) break;
            try { Thread.sleep(1000); } catch(InterruptedException e) {
                Log.warning("Executors shutdown interrupted");
            }
            Log.info("Waiting for place ingest executors to exit...");
        }

        if(!(httpExecutors != null && httpExecutors.isTerminated())) {
            Log.error("Failed to shutdown place ingest http executors");
        }
        if(!(dbExecutor != null && dbExecutor.isTerminated())) {
            Log.error("Failed to shutdown place ingest DB executor");
        }
        if(!(queueMonitor != null && queueMonitor.isTerminated())) {
            Log.error("Failed to shutdown place ingest queue monitor");
        }
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
                        configurationService.getIntValue(key), new NamedThreadFactory("http-executor"));
                for(Runnable task : awaitingTasks) {
                    httpExecutors.submit(task);
                }
            }
        } else if(key == ConfigurationKey.PLACE_INGEST_STOPPED) {
            stoppedFlag = true;
        }
    }    
    
    public void ingestByZip(String zipCode, String countryCode) {
        if(stoppedFlag) return;
        
        ZipIngest ingest = placeService.getZipIngest(zipCode, countryCode);
        if(ingest == null || ingest.getStatus() != ZipIngest.Status.N) return;
        
        if(Log.isInfoEnabled()) Log.info("Starting ingest for zip: " + zipCode);
        
        ingest.setStatus(Ingest.Status.I);
        placeService.updateZipIngest(ingest);
        
        List<String> categories = placeService.getAllPlaceCategoryNames();
        int countDown = categories.size();
        for(String category : categories) {
            synchronized (httpExecutors) {
                httpExecutors.submit(new HttpExecutor(zipCode, countryCode, category, countDown));
            }
        }
    }
    
    public Object[] ingestByZipAndCategory(
            String zipCode, String countryCode, String categoryOrTerm, Integer pageNum) {
        return getPlacesAndOptionallyIngest(zipCode, countryCode, categoryOrTerm, pageNum, null);
    } 
    
    private Object[] getPlacesAndOptionallyIngest(final String zipCode, final String countryCode,
            final String categoryOrTerm, Integer pageNum, List<Place> placeList) {
        
        final PlaceIngest ingest = placeService.getIngest(zipCode, countryCode, categoryOrTerm);
        if(ingest != null) {        
            PlaceIngest.Status status = ingest.getStatus();
            
            List<Place> places = placeList;
            boolean fromRemoteSource = false;
            
            if(status != PlaceIngest.Status.R) {
                boolean needPageOnly = (pageNum != null);
                
                if(needPageOnly) {
                    if(places == null) {
                        try {
                            places = getPlacesFromSource(zipCode, countryCode, categoryOrTerm, pageNum);
                        } catch (Exception e) {
                            Log.exception(e);
                            places = Collections.emptyList();
                        }
                        fromRemoteSource = true;
                        
                        if(status == PlaceIngest.Status.N) {
                            ExecutorService exec = Executors.newFixedThreadPool(1);
                            exec.submit(                            
                                new Runnable() {                                    
                                    @Override
                                    public void run() {
                                        try {
                                            List<Place> allPlaces = getPlacesFromSource(
                                                    zipCode, countryCode, categoryOrTerm, null);
                                            placeService.saveIngest(ingest, allPlaces);
                                        } catch (Exception e) {
                                            Log.exception(e);
                                        }
                                    }
                            });
                            exec.shutdown();                            
                        }
                    }
                } else {
                    if(places == null) {
                        places = getPlacesFromSource(zipCode, countryCode, categoryOrTerm, null);
                        fromRemoteSource = true;
                    }
                    if(status == PlaceIngest.Status.N) {
                        placeService.saveIngest(ingest, places);
                    }
                }
            } else {
                if(places == null) {
                    places = placeService.getPlaces(ingest);
                }
            }

            return new Object[]{places, fromRemoteSource};
        }
        return null;
    }
    
    private List<Place> getPlacesFromSource(
            String zipCode, String countryCode, String categoryOrTerm, Integer pageNum) {
        List<Place> places = (pageNum == null) ? 
                yellowPagesService.getPlaces(zipCode, categoryOrTerm) :
                yellowPagesService.getPageOfPlaces(zipCode, categoryOrTerm, pageNum, 
                        configurationService.getIntValue(ConfigurationKey.DIRECTORY_PAGE_SIZE));
        for(Place p : places) p.setCountryCode(countryCode);
        return places;
    }

}
