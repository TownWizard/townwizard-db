package com.townwizard.db.global.facebook.connect;

import static com.townwizard.db.constants.Constants.EMPTY_JSON;
import static com.townwizard.db.constants.Constants.FB_APP_ID;
import static com.townwizard.db.constants.Constants.FB_APP_SECRET;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.global.location.service.LocationService;
import com.townwizard.db.util.HttpUtils;
import com.townwizard.globaldata.model.Location;

@Component("facebookConnector")
public final class FacebookConnector {
    
    private static final String FQL_URL = "https://graph.facebook.com/fql?q=";
    private static final String PUBLIC_ACCESS_TOKEN_URL =
            "https://graph.facebook.com/oauth/access_token?" +
            "client_id=" + FB_APP_ID + "&client_secret=" + FB_APP_SECRET + 
            "&grant_type=client_credentials";
    
    private String publicAccessToken;
    
    @Autowired
    private LocationService locationService;
    
    /**
     * Execute fql and return facebook json as a result
     */
    public String executeFQL(String fql) 
            throws ClientProtocolException, UnsupportedEncodingException, IOException {
        return executeFBRequest(FQL_URL + URLEncoder.encode(fql, "UTF-8"));
    }
    
    public String executeLocationsRequest(Location location, Integer distanceInMeters) 
            throws ClientProtocolException, UnsupportedEncodingException, IOException {
        StringBuilder url = new StringBuilder();
        url.append("https://graph.facebook.com/search?type=place&center=")
            .append(location.getLatitude()).append(",")
            .append(location.getLongitude())
            .append("&distance=").append(distanceInMeters);
        return executeFBRequest(url.toString());
    }
    
    private String executeFBRequest(String url)
            throws ClientProtocolException, UnsupportedEncodingException, IOException {
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
    
    private void setPublicAccessToken() 
            throws ClientProtocolException, UnsupportedEncodingException, IOException {
        String response = HttpUtils.executeGetRequest(PUBLIC_ACCESS_TOKEN_URL);
        //response looks like "access_token=373685232723588|4x1XxnbQ9IXl5_S2XzOYqyyC4xw"
        String[] splitted = response.split("=");
        if(splitted.length > 1) {
            publicAccessToken = URLEncoder.encode(splitted[1], "UTF-8");         
        }
    }

    private String appendPublicToken(String url) {
        return url  + "&access_token=" + publicAccessToken;
    }

}
