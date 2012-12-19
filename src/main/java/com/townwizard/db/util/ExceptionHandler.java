package com.townwizard.db.util;

import com.townwizard.db.logger.Log;

/**
 * Application exception handler
 */
public final class ExceptionHandler {

    private ExceptionHandler() {}
    
    /**
     * Method to handle errors in the application.
     * All methods shoud call it for consistency of exception handling across the site
     */
    public static void handle(Exception e) {        
        Log.exception(e);
    }
}