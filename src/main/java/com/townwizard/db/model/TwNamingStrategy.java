package com.townwizard.db.model;

import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.internal.util.StringHelper;

/**
 * A class necessary to facilitate back and forth translation
 * between Java class and property names and DB table and column names with the minimal use of
 * annotations.
 * 
 * The instance of this class is added to the Hibernate configuration at the application startup
 */
public class TwNamingStrategy extends ImprovedNamingStrategy {
    
    private static final long serialVersionUID = 1L;

    @Override
    public String classToTableName(String className) {
        return StringHelper.unqualify(className);
    }
    
}