package com.townwizard.db.model.dto;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.townwizard.db.model.Content.ContentType;

/**
 * A "flat" version of an Rating object, more suitable for JSON rendering than the
 * Rating object
 */
@JsonSerialize (include = JsonSerialize.Inclusion.NON_EMPTY)
public class RatingDTO {
    
    private Long userId;
    private Integer siteId;
    private Long contentId;
    private Float value;
    private ContentType contentType;    
    private Integer count;
    
    public RatingDTO() {}
    
    public RatingDTO(Long userId, Integer siteId, Long contentId, Float value, ContentType contentType) {
        this.userId = userId;
        this.siteId = siteId;
        this.contentId = contentId;
        this.value = value;
        this.contentType = contentType;
    }

    public RatingDTO(Long userId, Integer siteId, Long contentId, Float value, ContentType contentType, Integer count) {
        this(userId, siteId, contentId, value, contentType);
        this.count = count;
    }    
    
    public Long getUserId() {
        return userId;
    }
    public Integer getSiteId() {
        return siteId;
    }
    public Long getContentId() {
        return contentId;
    }
    public Float getValue() {
        return value;
    }
    public ContentType getContentType() {
        return contentType;
    }
    public Integer getCount() {
        return count;
    }

    @JsonIgnore
    public boolean isValid() {
        return userId != null && siteId != null && contentId != null && value != null && contentType != null;
    }
    
    @JsonIgnore
    public boolean isValidAverage() {
        return siteId != null && contentId != null && value != null && contentType != null;
    }
    
}
