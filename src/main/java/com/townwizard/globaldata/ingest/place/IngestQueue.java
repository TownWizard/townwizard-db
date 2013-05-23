package com.townwizard.globaldata.ingest.place;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Component;

@Component("placeIngestQueue")
public final class IngestQueue {
    
    private static final Queue<IngestTask> queue = new ConcurrentLinkedQueue<>();
    
    public void enqueue(IngestTask task) {
        queue.add(task);
    }
    
    public IngestTask dequeue() {
        return queue.poll();
    }

}
