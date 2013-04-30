package com.townwizard.globaldata.service;

import java.util.ArrayList;
import java.util.List;

import com.townwizard.globaldata.model.Convertible;

/**
 * Helper methods for global data services
 */
public final class ServiceUtils {
    
    private ServiceUtils(){}
    
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
