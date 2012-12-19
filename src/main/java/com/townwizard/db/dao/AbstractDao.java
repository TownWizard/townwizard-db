package com.townwizard.db.dao;

import com.townwizard.db.model.AbstractEntity;

/**
 * Generic interface for all DAOs, which contains methods on common CRUD operations on entities
 */
public interface AbstractDao {
    <T extends AbstractEntity> Long create(T entity);    
    <T extends AbstractEntity> void delete(T entity);
    <T extends AbstractEntity> void update(T entity);  
    <T extends AbstractEntity> T getById(Class<T> clazz, Long id);
}