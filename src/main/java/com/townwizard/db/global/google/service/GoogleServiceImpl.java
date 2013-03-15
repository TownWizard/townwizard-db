package com.townwizard.db.global.google.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.global.google.connect.GoogleConnector;
import com.townwizard.db.global.google.model.GoogleLocation;
import com.townwizard.db.global.location.service.LocationService;
import com.townwizard.db.global.model.Convertible;
import com.townwizard.db.global.model.Location;
import com.townwizard.db.util.ReflectionUtils;

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
                    String json = connector.executePlacesNearbyRequest(
                            latitude.doubleValue(), longitude.doubleValue(), distanceInMeters, null, null);
                    
                    
                    List<GoogleLocation> gObjects = jsonToObjects(json, GoogleLocation.class);
                    List<Location> objects = convertList(gObjects);
                    return objects;
                    
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        return Collections.emptyList();        
    }
    
    
    private <T> List<T> jsonToObjects (String json, Class<T> objectClass) 
            throws JSONException, IllegalAccessException, InstantiationException {
        List<T> objects = new ArrayList<>();
        JSONObject j = new JSONObject(json);
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
