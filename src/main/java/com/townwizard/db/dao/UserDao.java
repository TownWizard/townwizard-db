package com.townwizard.db.dao;

import com.townwizard.db.model.LoginRequest;
import com.townwizard.db.model.User;
import com.townwizard.db.model.User.LoginType;

/**
 * Contains classes to manipulated users in the DB
 */
public interface UserDao extends AbstractDao {
    /**
     * Find a user by email and login type. 
     */
    User getByEmailAndLoginType(String email, LoginType loginType);
    
    /**
     *  Find a user by external id and login type.
     */
    User getByExternalIdAndLoginType(String externalId, LoginType loginType);
    
    /**
     * Create a login request object in the DB. Login request is an axillary DB object necessary
     * to facilitate Facebook login.
     * 
     * During FB login process a temporary object containing a unique id, original url, and a date
     * is inserted in the database, and it is removed after a successful login
     */
    void createLoginRequest(LoginRequest loginRequest);
    
    /**
     * Get login request by its unique identifier and delete its corresponding record in the DB,
     * so the method is called once per login
     */
    LoginRequest getLoginRequest(String uuid);
    
}