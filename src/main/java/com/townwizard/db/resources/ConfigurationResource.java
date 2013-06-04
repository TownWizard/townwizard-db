package com.townwizard.db.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.configuration.ConfigurationService;

@Component
@Path("/config")
public class ConfigurationResource extends ResourceSupport {
    
    @Autowired
    private ConfigurationService configurationService;

    @GET
    @Path("/save")
    @Produces(MediaType.TEXT_PLAIN)
    public String save(@QueryParam ("key") String key, @QueryParam("value") String value) {
        try {
            if(configurationService.exists(key)) {
                configurationService.save(key, value);
                return "success";
            }
            return "unknown key";            
        } catch (Exception e) {
            handleGenericException(e);
            return "failure";
        }
    }
    
    @GET
    @Path("/delete")
    @Produces(MediaType.TEXT_PLAIN)
    public String delete(@QueryParam ("key") String key) {
        try {
            if(configurationService.exists(key)) {
                configurationService.delete(key);
                return "success";
            }
            return "unknown key";
        } catch (Exception e) {
            handleGenericException(e);
            return "failure";
        }
    }    
    
}
