package com.townwizard.db.model;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.stereotype.Component;

/**
 * This class is used by hibernate to fill an entity active flag, created, and updated dates
 * before the object is saved in the DB
 * 
 * The instance of this class is added to the Hibernate configuration at the application startup
 */
@Component("entityInterceptor")
public class EntityInterceptor extends EmptyInterceptor {
    
    private static final long serialVersionUID = 1L;

    @Override
    public boolean onSave(Object o, Serializable id, Object[] state,
            String[] propertyNames, Type[] types) {

        if(o instanceof AbstractEntity) {
            AbstractEntity entity = (AbstractEntity)o;
            boolean isNew = (entity.getId() == null);
            Date now = new Date();
            
            for(int i = 0; i < propertyNames.length; i++) {
                String propertyName = propertyNames[i];
                if("active".equals(propertyName) && isNew) {
                    state[i] = true;
                } else if("updated".equals(propertyName) || ("created".equals(propertyName) && isNew)) {
                    state[i] = now;
                }
            }
            return true;
        }
        
        return false;
    }
    
    public boolean onFlushDirty(Object o, Serializable id,
            Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) 
            throws CallbackException {
        if(o instanceof AuditableEntity) {
            for(int i = 0; i < propertyNames.length; i++) {
                String propertyName = propertyNames[i];
                if("updated".equals(propertyName)) {
                    currentState[i] = new Date();
                }
            }
            return true;
        }
        return false;
    }
    
}