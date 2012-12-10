package com.townwizard.db.model.dto;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;

public class EventResponseDTO {
    private Long userId;
    private Integer siteId;
    private Long eventId;
    private Date eventDate;
    private Character value;
    
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public Character getValue() {
        return value;
    }

    public void setValue(Character value) {
        this.value = value;
    }
    
    @JsonIgnore
    public boolean isValid() {
        return userId != null && siteId != null && eventId != null && value != null;
    }
    
}
