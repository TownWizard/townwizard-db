package com.townwizard.globaldata.model.directory;

import java.util.Date;

import javax.persistence.Entity;

@Entity
public class ZipIngest extends Ingest {
    
    private Date started;
    private Date finished;

    public Date getStarted() {
        return started;
    }
    public void setStarted(Date started) {
        this.started = started;
    }
    public Date getFinished() {
        return finished;
    }
    public void setFinished(Date finished) {
        this.finished = finished;
    }
    
}
