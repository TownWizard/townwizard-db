package com.townwizard.db.logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.configuration.ConfigurationKey;
import com.townwizard.db.configuration.ConfigurationService;

/**
 * Centrilized place for logging methods.  This class is a thin wraper around Java logging system
 */
@Component
public final class Log {
    
    @Autowired private ConfigurationService configurationService;
    private static ConfigurationService config;
    
    private static Logger logger = Logger.getLogger("com.townwizard.db");
    private static java.util.logging.Formatter formatter = new Formatter();    
    static {
        List<java.util.logging.Handler> handlers = new ArrayList<>();
        Logger l = logger;
        do {
            handlers.addAll(Arrays.asList(l.getHandlers()));
            l = l.getParent();
        } while (l != null);
        for(java.util.logging.Handler h : handlers) {
            h.setFormatter(formatter);            
        }        
    }
    
    @PostConstruct
    public void init() {
        config = configurationService;
    }
    
    public static boolean isInfoEnabled() {
        return logger.isLoggable(Level.INFO);
    } 
    
    public static boolean isDebugEnabled() {
        return config.getBooleanValue(ConfigurationKey.LOG_DEBUG_ENABLED) && 
                logger.isLoggable(Level.FINE);
    }
    
    public static boolean isWarningEnabled() {
        return logger.isLoggable(Level.WARNING);
    }

    public static boolean isErrorEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }
    
    public static void info(String message) {
        logger.info(message);        
    }
    
    public static void debug(String message) {
        logger.fine(message);
    }
    
    public static void error(String message) {
        logger.severe(message);
    }
    
    public static void warning(String message) {
        logger.warning(message);
    }
    
    public static void log(Level level, String loggerName, String method, String message) {
        logger.logp(level, loggerName, method, message);
    }    
    
    public static void log(Level level, Class<?> clazz, String method, String message) {
        log(level, clazz.getSimpleName(), method, message);
    }
    
    
    /**
     * A convinience method to log exceptions.  Prints exception message
     * and a stack trace.
     */
    public static void exception(Throwable e) {
        if(isErrorEnabled()) {            
            Throwable c = e;
            Throwable cause = c;
            while((c = c.getCause()) != null) {
                cause = c;
            }
            error(cause.getMessage());
            StringBuilder stack = new StringBuilder();
            for(StackTraceElement elem : cause.getStackTrace()) {
                stack.append(elem.toString()).append("\n");
            }
            error(stack.toString());
        }
    }
}
