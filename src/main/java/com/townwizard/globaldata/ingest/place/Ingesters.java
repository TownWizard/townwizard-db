package com.townwizard.globaldata.ingest.place;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.annotation.PostConstruct;

import net.sf.ehcache.util.NamedThreadFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.configuration.ConfigurationKey;
import com.townwizard.db.configuration.ConfigurationListener;
import com.townwizard.db.configuration.ConfigurationService;
import com.townwizard.db.logger.Log;
import com.townwizard.globaldata.dao.PlaceDao;
import com.townwizard.globaldata.model.directory.Ingest;
import com.townwizard.globaldata.model.directory.PlaceCategory;
import com.townwizard.globaldata.model.directory.ZipIngest;
import com.townwizard.globaldata.service.PlaceService;

@Component("placeIngesters")
public final class Ingesters implements ConfigurationListener {

    private static final int HTTP_TASK_BATCH_SIZE = 10;

    private static ExecutorService ingestersLoop;
    private static ExecutorService dbLoop;
    private static ExecutorService ingestReporter;
    private static boolean stoppedFlag = false;
   
    private List<Ingester> ingesters = new CopyOnWriteArrayList<>();
    private List<Ingester> highPriorityIngesters = new CopyOnWriteArrayList<>();
    private List<Ingester> processedHighPriorityIngesters = new CopyOnWriteArrayList<>();
    private int currentIngesterIndex;

    @Autowired private IngestQueue placeIngestQueue;
    @Autowired private PlaceDao placeDao;
    @Autowired private PlaceService placeService;
    @Autowired private IngestHttpExecutors placeIngestHttpExecutors;
    @Autowired private ConfigurationService configurationService;

    @PostConstruct
    public void init() {
        ingestersLoop = Executors.newFixedThreadPool(1, new NamedThreadFactory("ingesters-loop"));
        ingestersLoop.submit(new IngestersLoop());
        Log.info("Place ingesters loop started");
        
        dbLoop = Executors.newFixedThreadPool(1, new NamedThreadFactory("db-loop"));
        dbLoop.submit(new DbLoop());
        Log.info("Place ingest db loop started");
        
        ingestReporter = Executors.newFixedThreadPool(1, new NamedThreadFactory("ingest-reporter"));
        ingestReporter.submit(new IngestReporter());
        Log.info("Place ingest reporter started");        
        
        configurationService.addConfigurationListener(this);
    }

    public void submitIngest(String zipCode, String countryCode) {
        if(stoppedFlag) return;
        
        ZipIngest ingest = placeService.getZipIngest(zipCode, countryCode);
        if(ingest == null || ingest.getStatus() != ZipIngest.Status.N) return;
        
        if(Log.isInfoEnabled()) {
            Log.info("Starting ingest for zip: " + zipCode);
        }
        
        List<PlaceCategory> categories = placeService.getAllPlaceCategories();        
        
        if(!ingestInProgress(zipCode, countryCode)) {
            ingesters.add(createIngester(zipCode, countryCode, categories, null));
        } else {
            Log.warning("Rejected intester for zip: (" + zipCode + ", " + countryCode + ")");
        }
    }
    
    public void submitHighPriorityIngest(String zipCode, String countryCode, String categoryOrTerm, 
            List<PlaceCategory> categories) {
        if(!hightPriorityInProgress(zipCode, countryCode, categoryOrTerm)) {
            highPriorityIngesters.add(createIngester(zipCode, countryCode, categories, categoryOrTerm));
        }
    }
    
    @Override
    public ConfigurationKey[] keysOfInterest() {
        return new ConfigurationKey[] {ConfigurationKey.PLACE_INGEST_STOPPED};        
    }

    @Override
    public void configurationChanged(ConfigurationKey key) {
        if(key == ConfigurationKey.PLACE_INGEST_STOPPED) {
            stoppedFlag = configurationService.getBooleanValue(ConfigurationKey.PLACE_INGEST_STOPPED);
        }
    }     
    
    private Ingester getNextIngester() {
        if(!ingesters.isEmpty()) {
            if(++currentIngesterIndex >= ingesters.size()) {
                currentIngesterIndex = 0;
            }
            return ingesters.get(currentIngesterIndex);
        }
        return null;
    }
        
    private final class IngestersLoop implements Runnable {
        @Override
        public void run() {
            while(true) {
                if(Thread.interrupted()) return;
                if(stoppedFlag) {                    
                    ingesters.clear();
                    placeIngestQueue.clear();
                }
                try {
                    boolean doneSomeWork = false;
                    if(!highPriorityIngesters.isEmpty()) {
                        for(Ingester ingester : highPriorityIngesters) {
                            placeIngestQueue.addHttpTask(
                                    new IngestTask(ingester.getZipCode(), ingester.getCountryCode(),
                                            ingester.getNextCategory(), true, null));
                        }
                        processedHighPriorityIngesters.addAll(highPriorityIngesters);
                        highPriorityIngesters.clear();
                        doneSomeWork = true;
                    }
                    
                    if(!processedHighPriorityIngesters.isEmpty()) {
                        for(Ingester ingester : processedHighPriorityIngesters) {
                            if(ingester.allDone()) {
                                processedHighPriorityIngesters.remove(ingester);
                                doneSomeWork = true;
                            }
                        }
                    }
                    
                    if(placeIngestQueue.submittedHttpTasks() < HTTP_TASK_BATCH_SIZE) {
                        int k = 0;
                        while(k++ < HTTP_TASK_BATCH_SIZE) {
                            Ingester i = getNextIngester();
                            if(i != null) {
                                if(i.hasNextCategory()) {
                                    String next = i.getNextCategory();
                                    placeIngestQueue.addHttpTask(
                                            new IngestTask(i.getZipCode(), i.getCountryCode(), next, false, null));
                                    doneSomeWork = true;
                                }
                            }
                        }
                    }
                    
                    for(Ingester ingester : ingesters) {
                        if(ingester.allDone()) {
                            ZipIngest zipIngest = placeService.getZipIngest(
                                    ingester.getZipCode(), ingester.getCountryCode());
                            zipIngest.setStatus(Ingest.Status.R);
                            zipIngest.setFinished(new Date());
                            placeService.updateZipIngest(zipIngest);
                            Log.info("Finished place ingest for zip: (" + 
                                    ingester.getZipCode() + ", " + ingester.getCountryCode() + ")");
                            ingesters.remove(ingester);
                            doneSomeWork = true;
                        }
                    }
                    
                    if(!doneSomeWork){
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
    
    private final class DbLoop implements Runnable {
        @Override
        public void run() {
            while(true) {
                if(Thread.interrupted()) return;
                try {
                    IngestTask task = placeIngestQueue.getDbTask();
                    if(task != null) {                        
                        Ingester ingester = findIngester(task);
                        if(ingester != null) {
                            ingester.ingest(task);
                        }
                    } else {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ie) {
                            Log.info("Exiting place db loop...");
                            return;
                        }
                    }
                } catch (Exception e) {
                    Log.exception(e);
                }
            }
        }
    }
    
    private final class IngestReporter implements Runnable {
        @Override
        public void run() {
            while (true) {
                if(Thread.interrupted()) return;
                try {
                    if(!ingesters.isEmpty() && Log.isDebugEnabled()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("\n---------------------------------\n");
                        sb.append("|   Zip   |   Done   |   Left   |\n");
                        sb.append("---------------------------------\n");
                        for(Ingester i : ingesters) {
                            int done = i.done();
                            sb.append("|")
                              .append(String.format("%7s", i.getZipCode())).append("  | ")
                              .append(String.format("%6d", done)).append("   | ")
                              .append(String.format("%6d", (i.size() - done))).append("   |\n");
                        }
                        sb.append("---------------------------------");
                        
                        Log.log(Level.FINE, getClass(), null, sb.toString());
                    }
                    
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException ie) {
                        Log.info("Exiting place ingest reporter ...");
                        return;
                    }                    
                } catch (Exception e) {
                    Log.exception(e);
                }
            }
        }
    }
    
    public static void shutdownThreads() {
        if(ingestersLoop != null) {        
            Log.info("About to shutdown place ingesters loop ...");
            ingestersLoop.shutdownNow();
            try {
                if(!ingestersLoop.awaitTermination(30, TimeUnit.SECONDS)) {
                    Log.warning("Cannot terminate place ingesters loop");
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }            
        }
        if(dbLoop != null) {
            Log.info("About to shutdown place ingest db loop ...");
            dbLoop.shutdownNow();
            try {
                if(!dbLoop.awaitTermination(30, TimeUnit.SECONDS)) {
                    Log.warning("Cannot terminate place ingest db loop");
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }            
        }
        if(ingestReporter != null) {
            Log.info("About to shutdown place ingest reporter ...");
            ingestReporter.shutdownNow();
            try {
                if(!ingestReporter.awaitTermination(30, TimeUnit.SECONDS)) {
                    Log.warning("Cannot terminate place ingest reporter");
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }            
        }        
    }
    
    private Ingester findIngester(IngestTask task) {
        Ingester result = null;
        if(task.isHighPriority()) {
            for(Ingester i : highPriorityIngesters) {
                if(i.getZipCode().equals(task.getZipCode()) && 
                   i.getCountryCode().equals(task.getCountryCode()) &&
                   i.getNextCategory().equals(task.getCategory())) {
                    result = i;
                    break;
                }
            }            
            for(Ingester i : processedHighPriorityIngesters) {
                if(i.getZipCode().equals(task.getZipCode()) && 
                   i.getCountryCode().equals(task.getCountryCode()) &&
                   i.getNextCategory().equals(task.getCategory())) {
                    result = i;
                    break;
                }
            }
        } else {
            for(Ingester i : ingesters) {
                if(i.getZipCode().equals(task.getZipCode()) &&
                   i.getCountryCode().equals(task.getCountryCode())) {
                    result = i;
                    break;
                }
            }
        }
        return result;
    }
    
    private boolean ingestInProgress(String zipCode, String countryCode) {
        return findIngester(new IngestTask(zipCode, countryCode, null, false, null)) != null;
    }
    
    private boolean hightPriorityInProgress(String zipCode, String countryCode, String categoryOrTerm) {
        return findIngester(new IngestTask(zipCode, countryCode, categoryOrTerm, true, null)) != null;
    }    
    
    private Ingester createIngester(String zipCode, String countryCode, List<PlaceCategory> categories,
            String categoryOrTerm) {
        return new JdbcIngester(zipCode, countryCode, categories, categoryOrTerm, placeDao);
    }
}
