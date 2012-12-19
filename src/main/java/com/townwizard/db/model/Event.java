package com.townwizard.db.model;

/**
 * A class representing a content of type event.
 * Event is a content with one additional attribute: date
 */
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
