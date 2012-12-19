package com.townwizard.db.dao;

import java.util.List;

import com.townwizard.db.model.Content;
import com.townwizard.db.model.Content.ContentType;

/**
 * Contains methods to manipulate Contents in the DB.
 * 
 * By content we mean any townwizard object which can be rated, favorited, reviewed, etc, and has
 * and ID in the townwizard database.  This id is referred to as as external content id.
 * 
 * Since in the townwizard database different contents can be located in different tables, it is not
 * enough to have just an external content id since it maybe duplicated across different tables, and
 * the notion of a content type is also introduced.
 * 
 * The example content types are locations, events, etc.
 * 
 * From Townwizard DB point of view a piece of content can be identified by a composite key as
 * (site id, content type, external content id)
 */
public interface ContentDao extends AbstractDao {
    
    /**
     * Given site id, content type, and external content id, return the content.  If none found,
     * return null
     */
    Content getContent(Integer siteId, ContentType contentType, Long externalContentId);
    
    /**
     * Given site id, content type, and list of content ids, return a list of contents.
     * If for a specific content id no content is found, do not include null in the list.
     * Return an empty list (not null) in case when no contents found in the DB.
     */
    List<Content> getContents(Integer siteId, ContentType contentType, List<Long>externalContentIds);
    
}