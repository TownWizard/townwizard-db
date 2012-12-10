package com.townwizard.db.model;

import java.util.Date;

import javax.persistence.Entity;

@Entity
public class Event extends Content {
    
    private static final long serialVersionUID = 4082922462876495342L;
    
    private Date date;
    
    public Event() {
        setContentType(ContentType.EVENT);
    }
    
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }       

}
