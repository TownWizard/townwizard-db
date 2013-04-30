package com.townwizard.db.util;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public final class JSONUtils {
    
    private JSONUtils(){}

    /**
     * Takes an JSONArray object, and the target object class; converts json object into a list 
     * of target objects.
     */
    public static <T> List<T> jsonToObjects (JSONArray data, Class<T> objectClass) 
            throws IllegalAccessException, InstantiationException {
        List<T> objects = new ArrayList<>();
        for(int i = 0; i < data.length(); i++) {
            JSONObject o = data.optJSONObject(i);
            @SuppressWarnings("cast")
            T object = (T)objectClass.newInstance();
            ReflectionUtils.populateFromJson(object, o);
            objects.add(object);
        }
        return objects;
    }

}
