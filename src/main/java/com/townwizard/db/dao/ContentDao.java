package com.townwizard.db.dao;

import java.util.List;

import com.townwizard.db.model.Content;
import com.townwizard.db.model.Content.ContentType;

public interface ContentDao extends AbstractDao {
    
    Content getContent(Integer siteId, ContentType contentType, Long externalContentId);
    
    List<Content> getContents(Integer siteId, ContentType contentType, List<Long>externalContentIds);
    
}