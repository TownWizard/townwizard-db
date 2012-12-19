package com.townwizard.db.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * A class representing login request.
 * Note that this class is not an AbstractEntity subclass, and it doesn't have all it's properties.
 * It's mapped to a table with three columns: id, date, and location, where location means the original
 * user URL from which the user is trying to login to the system.
 */
@Entity
public class LoginRequest implements Serializable {    
    
    private static final long serialVersionUID = 7725530715315270137L;
    
    @Id
    private String id;
    private String location;
    private Date date;
    
    public LoginRequest(){}
    
    public LoginRequest(String id, String location, Date date) {
        this.id = id;
        this.location = location;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public Date getDate() {
        return date;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    
}