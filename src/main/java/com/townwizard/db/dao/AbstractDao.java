package com.townwizard.db.dao;

/**
 * Generic interface for all DAOs, which contains methods on common CRUD operations on entities
 */
public interface AbstractDao {
    <T> Long create(T entity);    
    <T> void delete(T entity);
    <T> void update(T entity);  
    <T> T getById(Class<T> clazz, Long id);
}