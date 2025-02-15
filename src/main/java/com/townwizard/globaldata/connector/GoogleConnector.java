package com.townwizard.globaldata.connector;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.springframework.stereotype.Component;

import com.townwizard.db.constants.Constants;
import com.townwizard.db.util.CollectionUtils;
import com.townwizard.db.util.HttpUtils;

/**
 * This class is responsible for executing Google HTTP queries
 */
@Component("googleConnector")
public final class GoogleConnector {
    
    private static final String PLACES_NEARBY_SEARCH_URL = 
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    
    private static final String PLACE_DETAILS_URL = 
            "https://maps.googleapis.com/maps/api/place/details/json?";
    
    /**
     * Execute request to get nearby places
     */
    public String executePlacesNearbyRequest(
            double latitude, double longitude, int distance, String name, List<String> types) 
        throws ClientProtocolException, IOException {
        
        StringBuilder sb = new StringBuilder(PLACES_NEARBY_SEARCH_URL);
        sb.append("location=").append(latitude).append(",").append(longitude)
          .append("&radius=").append(distance);
        if(name != null) {
            sb.append("&name=").append(name);
        }
        if(types != null && !types.isEmpty()){
            sb.append("&types").append(CollectionUtils.join(types, "|", null));
        }
        
        appendMandatoryParameters(sb);
        
        String url = sb.toString();
        String response = HttpUtils.executeGetRequest(url);
        return response;
    }

    /**
     * Execute request to get next page of places.
     * It can be execute no more the twice per places request.
     */
    public String executePlacesNearbyPageTokenRequest(String pageToken) 
        throws ClientProtocolException, IOException {
        StringBuilder sb = new StringBuilder(PLACES_NEARBY_SEARCH_URL);
        sb.append("pagetoken=").append(pageToken);
        appendMandatoryParameters(sb);
        String response = HttpUtils.executeGetRequest(sb.toString());
        return response;
    }
    
    /**
     * Execute request to get a particular place details by reference obtained from the previous
     * places request.
     */
    public String executePlaceDetailRequest(String reference) 
        throws ClientProtocolException, IOException {
        StringBuilder sb = new StringBuilder(PLACE_DETAILS_URL);
        sb.append("reference=").append(reference);
        appendMandatoryParameters(sb);
        String response = HttpUtils.executeGetRequest(sb.toString());
        return response;
    }
    
    private void appendMandatoryParameters(StringBuilder sb) {
        sb.append("&sensor=false").append("&key=").append(Constants.GOOGLE_API_KEY);
    }

}
