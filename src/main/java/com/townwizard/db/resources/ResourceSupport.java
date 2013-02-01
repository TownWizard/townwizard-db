package com.townwizard.db.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;

import com.townwizard.db.services.UserService;
import com.townwizard.db.util.ExceptionHandler;
import com.townwizard.db.util.jackson.NullStringDeserializer;

/**
 * A common superclass for all classes which represent REST endpoints
 */
public abstract class ResourceSupport {
    
    protected static final String FB_APP_ID = "373685232723588";
    protected static final String FB_APP_SECRET = "d9c84a8e96b422fe8075360a8415f584";
    protected static final String TWITTER_APP_ID = "nkmH275UmsX63Zg1JP0Ejw";
    protected static final String TWITTER_APP_SECRET = "cjxlpRfxn4Au1aEnIM8ETggH9Y77dy0otxFucJxMA";
    
    protected static final String EMPTY_JSON = "{}";
    
    @Autowired
    private UserService userService;
    private static ObjectMapper objectMapper = initializeObjectMapper();    
    
    /**
     * Translates a generic exception into an HTTP response with code 500 (server error)
     * and send it to the client.
     * 
     * All endpoints should be using this method to report error to clients in order to maintain
     * consistency in handling errors accross the site
     */
    protected void sendServerError(Exception e) {
        throw new WebApplicationException(Response
                .status(Status.INTERNAL_SERVER_ERROR)
                .entity("Server error: " + e.getMessage())
                .type(MediaType.TEXT_PLAIN).build());
    }
    
    /**
     * Generic exception handler for all endpoints.
     * This method logs the error, as well as reports it to the client
     */
    protected void handleGenericException(Exception e) {
        if(!(e instanceof WebApplicationException)) {
            ExceptionHandler.handle(e);
            sendServerError(e);
        } else {
            throw (WebApplicationException)e;
        }
    } 
    
    /**
     * Generic request to json parsers available to all services
     */
    protected <T> T parseJson(Class<T> entityClass, InputStream is) {
        T entity = null;
        try {
            entity = objectMapper.readValue(is, entityClass);
          } catch (JsonProcessingException e) {
              ExceptionHandler.handle(e);
              throw new WebApplicationException(Response
                      .status(Status.BAD_REQUEST).entity("Cannot parse JSON: " + e.getMessage())
                      .type(MediaType.TEXT_PLAIN).build());
          } catch (IOException e) {
              handleGenericException(e);
              return null;
          }
        return entity;
    }
    
    /**
     * Json to java map parser
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> parseJson(String json) throws IOException, JsonProcessingException {      
        return objectMapper.readValue(json, HashMap.class);
    }
    
    private static ObjectMapper initializeObjectMapper() {
        ObjectMapper m = new ObjectMapper();
        SimpleModule testModule = new SimpleModule("MyModule", new Version(1, 0, 0, null))
                     .addDeserializer(String.class, new NullStringDeserializer());
        m.registerModule(testModule);
        return m;
    }
    
}
