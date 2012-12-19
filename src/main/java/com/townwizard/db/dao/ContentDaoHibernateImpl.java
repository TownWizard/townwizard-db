package com.townwizard.db.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.townwizard.db.model.Content;
import com.townwizard.db.model.Content.ContentType;

/**
 * Hibernate implementation of ContentDao interface.
 */
@Component("contentDao")
public class ContentDaoHibernateImpl extends AbstractDaoHibernateImpl implements ContentDao {

    @Override
    public Content getContent(Integer siteId, ContentType contentType, Long externalContentId) {
        return (Content)getSession().createQuery(
                "from Content where " + 
                "externalId = :external_id and siteId = :site_id and contentType = :type and active = true")
                .setLong("external_id", externalContentId)
                .setInteger("site_id", siteId)
                .setInteger("type", contentType.getId()).uniqueResult();        
    }

    
    @Override
    public List<Content> getContents(Integer siteId, ContentType contentType, 
            List<Long> externalContentIds) {
        @SuppressWarnings("unchecked")    
        List<Content> contents = getSession().createQuery(
                "from Content where " + 
                "externalId in :external_ids and siteId = :site_id and contentType = :type and active = true")
                .setParameterList("external_ids", externalContentIds)
                .setInteger("site_id", siteId)
                .setInteger("type", contentType.getId()).list();
        return contents;
    }
    
}
