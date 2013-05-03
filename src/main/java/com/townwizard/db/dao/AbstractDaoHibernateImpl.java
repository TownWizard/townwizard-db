package com.townwizard.db.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Required;

import com.townwizard.db.model.AbstractEntity;

/**
 * Abstract class which implements AbstracDao methods (CRUD operations), using Hibernate
 * This class also makes hibernate session available to all subclasses.
 */
public abstract class AbstractDaoHibernateImpl implements AbstractDao {
    
    private SessionFactory sessionFactory;

    /**
     * Return current session.
     * 
     * This method needs to be called in every DAO method wishing to 
     * communicate with the DB.
     * 
     * The session returned is "current", meaning it was open outside as a part of an ongoing
     * transaction.  The method will not create a new session if there is no transaction in progress, 
     * instead it will through a hibernate exception     
     */
    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }
    
    public <T> Long create(T entity) {
        return (Long) getSession().save(entity);
    }
    
    public <T> void update(T entity) {
        getSession().update(entity);
    }
    
    public <T> void delete(T entity) {
        getSession().delete(entity);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getById(Class<T> klass, Long id) {
        T entity = (T)getSession().get(klass, id);
        if (entity != null) {
            if(entity instanceof AbstractEntity) {
                if(Boolean.FALSE.equals(((AbstractEntity)entity).getActive())) {
                    entity = null;
                }
            }
        }
        return entity;
    }
    
    @Required
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}