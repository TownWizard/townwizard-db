package com.townwizard.globaldata.service;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.logger.Log;
import com.townwizard.db.util.JSONUtils;
import com.townwizard.globaldata.connector.GoogleConnector;
import com.townwizard.globaldata.model.Google;
import com.townwizard.globaldata.model.directory.Location;

/**
 * GoogleService implementation
 */
@Component("googleService")
public class GoogleServiceImpl implements GoogleService {
    
    @Autowired
    private GoogleConnector connector;

    /**
     * May execute from one to three http requests to Google.
     * First request goes to the location url; if the json returned has next page token,
     * one or two more requests will be executed to get more locations.
     */
    @Override
    public List<Location> getLocations(double latitude, double longitude, int distanceInMeters) {
        try {
            List<Location> finalList = new ArrayList<>(20);

            String json = connector.executePlacesNearbyRequest(
                    latitude, longitude, distanceInMeters, null, null);

            JSONObject j = new JSONObject(json);
            List<Google.Location> gObjects = jsonToObjects(j, Google.Location.class);
            finalList.addAll(ServiceUtils.convertList(gObjects));

            int i = 0;
            while (true) {
                if (Log.isDebugEnabled()) Log.debug("Executing next page request request " + (++i));
                String nextPageToken = j.optString("next_page_token");
                if (nextPageToken == null || nextPageToken.isEmpty() || i > 10) break;
                json = connector.executePlacesNearbyPageTokenRequest(nextPageToken);
                j = new JSONObject(json);
                gObjects = jsonToObjects(j, Google.Location.class);
                finalList.addAll(ServiceUtils.convertList(gObjects));
            }
            return finalList;            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }    
    
    private <T> List<T> jsonToObjects (JSONObject j, Class<T> objectClass) 
            throws JSONException, IllegalAccessException, InstantiationException {
        JSONArray data = j.getJSONArray("results");
        return JSONUtils.jsonToObjects(data, objectClass);
    }

}
