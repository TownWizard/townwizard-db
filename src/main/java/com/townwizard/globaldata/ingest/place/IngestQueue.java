package com.townwizard.globaldata.ingest.place;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import net.sf.ehcache.util.NamedThreadFactory;

import org.springframework.stereotype.Component;

import com.townwizard.db.logger.Log;

@Component("placeIngestQueue")
public final class IngestQueue {
    
    private static final Queue<IngestTask> httpExecutorsQueue = new ConcurrentLinkedQueue<>();
    private static final Queue<IngestTask> highPrioryHttpExecutorsQueue = new ConcurrentLinkedQueue<>();
    private static final Queue<IngestTask> dbIngestQueue = new ConcurrentLinkedQueue<>();
    
    private static boolean shutdownFlag;
    private static ExecutorService queueMonitor;
    
    public void addHttpTask(IngestTask task) {
        if(task.isHighPriority()) {
            boolean found = false;
            for(IngestTask t : highPrioryHttpExecutorsQueue) {
                if(t.equals(task)) found = true;
                break;
            }
            if(!found) {
                highPrioryHttpExecutorsQueue.add(task);
            }            
        } else {
            httpExecutorsQueue.add(task);
        }
    }
    
    public IngestTask getHttpTask() {
        return httpExecutorsQueue.poll();
    }
    
    public IngestTask getHighPriorityHttpTask() {
        return highPrioryHttpExecutorsQueue.poll();
    }    
    
    public void addDbTask(IngestTask task) {
        dbIngestQueue.add(task);
    }
    
    public IngestTask getDbTask() {
        return dbIngestQueue.poll();
    }
    
    public int submittedHttpTasks() {
        return httpExecutorsQueue.size();
    }
    
    public void clear() {
        httpExecutorsQueue.clear();
        dbIngestQueue.clear();
    }
    
    @PostConstruct
    public void init() {
        queueMonitor = Executors.newFixedThreadPool(1, new NamedThreadFactory("queue-monitor"));
        queueMonitor.submit(new IngestQueueMonitor());
    }
    
    public static void shutdownThreads() {
        if(queueMonitor != null) {
            Log.info("About to shutdown place ingest queue monitor...");
            queueMonitor.shutdownNow();
            
            try {
                if(!queueMonitor.awaitTermination(30, TimeUnit.SECONDS)) {
                    Log.warning("Cannot terminate place ingest queue monitor");
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
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
                    int httpQueueSize = httpExecutorsQueue.size();
                    int dbQueueSize = dbIngestQueue.size();
                    if(httpQueueSize > 0) {
                        if(Log.isInfoEnabled()) {
                            Log.info("Ingest http executor queue size: " + httpQueueSize);
                        }
                    }
                    if(dbQueueSize > 0) {
                        if(Log.isInfoEnabled()) {
                            Log.info("Db ingest queue size: " + dbQueueSize);
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

}
