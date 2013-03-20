package com.townwizard.globaldata.service;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.util.ReflectionUtils;
import com.townwizard.globaldata.connector.YellowPagesConnector;
import com.townwizard.globaldata.model.Convertible;
import com.townwizard.globaldata.model.Location;
import com.townwizard.globaldata.model.yellopages.YPLocation;

@Component("yellowPagesService")
public class YellowPagesServiceImpl implements YellowPagesService {
    
    @Autowired
    private YellowPagesConnector connector;

    @Override
    public List<Location> getLocations(String term, String zip, Integer distanceInMeters) {
        try {
            String json = connector.executePlacesRequest(term, zip, distanceInMeters);
            List<YPLocation> gObjects = jsonToObjects(json, YPLocation.class);
            List<Location> objects = convertList(gObjects);
            return objects;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private <T> List<T> jsonToObjects (String json, Class<T> objectClass) 
            throws JSONException, IllegalAccessException, InstantiationException {
        List<T> objects = new ArrayList<>();
        JSONObject j = new JSONObject(json);
        JSONObject searchResult = j.optJSONObject("searchResult");
        if(searchResult != null) {
            JSONObject searchListings = searchResult.optJSONObject("searchListings");
            if(searchListings != null) { 
                JSONArray data = searchListings.getJSONArray("searchListing");        
                for(int i = 0; i < data.length(); i++) {
                    JSONObject o = data.optJSONObject(i);
                    @SuppressWarnings("cast")
                    T object = (T)objectClass.newInstance();
                    ReflectionUtils.populateFromJson(object, o);
                    objects.add(object);
                }
            }
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
