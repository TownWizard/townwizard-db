package com.townwizard.db.resources;

import java.net.URLEncoder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.stereotype.Component;

import com.townwizard.db.util.HttpUtils;

@Component
@Path("/fb")
public class FBResource extends ResourceSupport {
    
    private static final String FQL_URL = "https://graph.facebook.com/fql?q=";
    private static final String PUBLIC_ACCESS_TOKEN_URL =
            "https://graph.facebook.com/oauth/access_token?" +
            "client_id=" + FB_APP_ID + "&client_secret=" + FB_APP_SECRET + 
            "&grant_type=client_credentials";    
    
    private String publicAccessToken;    
    
    @GET
    @Path("/events")
    @Produces(MediaType.APPLICATION_JSON)
    public Response events() {
        try {
            String json = getPublicEvents();            
            return Response.status(Status.OK).entity(json).build();
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }    
    
    private String getPublicEvents() throws Exception {        
//        String url = "https://graph.facebook.com/search?" +
//        		"q=party&type=event&center=37.76,-122.427&distance=1000";
        String fql = "SELECT name FROM event WHERE contains('Boom Party')";
        return executeFQL(fql);                
    }
    
    private void setPublicAccessToken() throws Exception {
        String response = HttpUtils.executeGetRequest(PUBLIC_ACCESS_TOKEN_URL);
        //response is "access_token=373685232723588|4x1XxnbQ9IXl5_S2XzOYqyyC4xw"
        String[] splitted = response.split("=");
        if(splitted.length > 1) {
            publicAccessToken = URLEncoder.encode(splitted[1], "UTF-8");         
        }
    }
    
    private String executeFQL(String fql) throws Exception {
        return executeFBRequest(FQL_URL + URLEncoder.encode(fql, "UTF-8"));
    }
    
    private String executeFBRequest(String url) throws Exception {
        if(publicAccessToken == null) {
           setPublicAccessToken();
        }
        
        String response = HttpUtils.executeGetRequest(appendPublicToken(url));
        if(isTokenRequiredResponse(response)) {
            setPublicAccessToken();
            response = HttpUtils.executeGetRequest(appendPublicToken(url));
            if(response.contains("\"error\":")) {
                publicAccessToken = null;
                return EMPTY_JSON;
            }
        }
        return response;
    }
    
    private boolean isTokenRequiredResponse(String response) {
        return response.contains("An access token is required");
    }
    
    private String appendPublicToken(String url) {
        return url  + "&access_token=" + publicAccessToken;
    }

}
