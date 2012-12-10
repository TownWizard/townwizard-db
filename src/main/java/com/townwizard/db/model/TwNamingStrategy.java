package com.townwizard.db.model;

import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.internal.util.StringHelper;

public class TwNamingStrategy extends ImprovedNamingStrategy {
    
    private static final long serialVersionUID = 1L;

    @Override
    public String classToTableName(String className) {
        return StringHelper.unqualify(className);
    }
    
}