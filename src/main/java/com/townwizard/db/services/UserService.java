package com.townwizard.db.services;

import com.townwizard.db.model.LoginRequest;
import com.townwizard.db.model.User;
import com.townwizard.db.model.User.LoginType;

/**
 * Service interface with methods to manage users.
 */
public interface UserService {
    /**
     * Get user by Id.  Return null if no user found
     */
    User getById(Long id);
    
    /**
     * Get user by email, and login type.  Return null if no user found.
     * Throws exception if email is null.
     */
    User getByEmailAndLoginType(String email, LoginType loginType);
    
    /**
     * Get user by externalId and login type. Return null if no user found.
     * Throws exception when externalId is null 
     */
    User getByExternalIdAndLoginType(Long externalId, LoginType loginType);

    /**
     * Get a townwizard user (a user with login type TOWNWIZARD) by email and password.
     * If no users found (password or email incorrect), return null
     * Throws exception if email or password is null
     */
    User login(String email, String password);
    
    /**
     * Create a user
     */
    Long create(User user);
    
    /**
     * Update a user
     */
    void update(User user);
    
    /**
     * Create login request
     */
    void createLoginRequest(LoginRequest loginRequest);
    
    /**
     * Get login request by its id and delete the request if found
     */
    LoginRequest getLoginRequest(String uuid);
}
