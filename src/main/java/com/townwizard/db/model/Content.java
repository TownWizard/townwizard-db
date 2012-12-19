package com.townwizard.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * A model class for content objects, where content is anything which can be rated, favorited,
 * reviewed and so on by a user.
 * 
 * Every content has a content type, a site id, and an external id which together comprise a unique
 * content identifier.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Content extends AbstractEntity {     
    
    /**
     * Enuration representing available content types.  This is mapped to a type_id column in the
     * content table.
     */
    public static enum ContentType {
        ZERO(0, "Zero"), //to make sure Java Enum ordinals will start with 1 for hibernate mapping
        LOCATION(1, "Location"),
        EVENT(2, "Event");
        
        private final int id;
        private final String name;
        private ContentType(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public int getId() {return id;}
        public String getName() {return name;}        
    }    
    
    private static final long serialVersionUID = -5577386542915432817L;

    private Long externalId;
    private Integer siteId;
    @Column(name="type_id")
    @Enumerated(EnumType.ORDINAL)
    private ContentType contentType;
    
    public Long getExternalId() {
        return externalId;
    }
    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }
    public Integer getSiteId() {
        return siteId;
    }
    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }
    public ContentType getContentType() {
        return contentType;
    }
    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }
    
}