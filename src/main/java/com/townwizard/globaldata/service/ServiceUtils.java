package com.townwizard.globaldata.service;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.townwizard.db.util.ReflectionUtils;
import com.townwizard.globaldata.model.Convertible;

/**
 * Helper methods for global data services
 */
public final class ServiceUtils {
    
    private ServiceUtils(){}
    
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

    /**
     * Takes a list of convertible objects, and returns a list of of target objects
     */
    public static <T> List<T> convertList(List<? extends Convertible<T>> gObjects) {
        List<T> objects = new ArrayList<>(gObjects.size());
        for(Convertible<T> c : gObjects) {
            objects.add(c.convert());
        }
        return objects;
    }

}
