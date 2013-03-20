package com.townwizard.globaldata.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.logger.Log;
import com.townwizard.db.util.ReflectionUtils;
import com.townwizard.globaldata.connector.GoogleConnector;
import com.townwizard.globaldata.model.Convertible;
import com.townwizard.globaldata.model.Google;
import com.townwizard.globaldata.model.Location;

@Component("googleService")
public class GoogleServiceImpl implements GoogleService {
    
    @Autowired
    private GoogleConnector connector;
    @Autowired
    private LocationService locationService;


    @Override
    public List<Location> getLocations(String zip, String countryCode, Integer distanceInMeters) {
        try {
            List<Location> zipLocations = locationService.getLocations(zip, countryCode);
            if(zipLocations != null && !zipLocations.isEmpty()) {
                Location zipLocation = zipLocations.get(0);
                Float latitude = zipLocation.getLatitude();
                Float longitude = zipLocation.getLongitude();
                if(latitude != null && longitude != null) {                    
                    
                    List<Location> finalList = new ArrayList<>(20);
                    
                    String json = connector.executePlacesNearbyRequest(
                            latitude.doubleValue(), longitude.doubleValue(), distanceInMeters, null, null);

                    JSONObject j = new JSONObject(json);           
                    List<Google.Location> gObjects = jsonToObjects(j, Google.Location.class);
                    finalList.addAll(convertList(gObjects));
                    
                    int i = 0;
                    while(true) {
                        if(Log.isDebugEnabled()) Log.debug("Executing next page request request " + (++i));
                        String nextPageToken = j.optString("next_page_token");
                        if(nextPageToken == null || nextPageToken.isEmpty() || i > 10) break;                        
                        json = connector.executePlacesNearbyPageTokenRequest(nextPageToken);
                        j = new JSONObject(json);
                        gObjects = jsonToObjects(j, Google.Location.class);
                        finalList.addAll(convertList(gObjects));                        
                    }
                    
                    return finalList;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        return Collections.emptyList();        
    }
    
    
    private <T> List<T> jsonToObjects (JSONObject j, Class<T> objectClass) 
            throws JSONException, IllegalAccessException, InstantiationException {
        List<T> objects = new ArrayList<>();
        JSONArray data = j.getJSONArray("results");        
        for(int i = 0; i < data.length(); i++) {
            JSONObject o = data.optJSONObject(i);
            @SuppressWarnings("cast")
            T object = (T)objectClass.newInstance();
            ReflectionUtils.populateFromJson(object, o);
            objects.add(object);
        }
        return objects;
    }
    
    private <T> List<T> convertList(List<? extends Convertible<T>> gObjects) {
        List<T> objects = new ArrayList<>(gObjects.size());
        for(Convertible<T> c : gObjects) {
            objects.add(c.convert());
        }
        return objects;
    }
}
