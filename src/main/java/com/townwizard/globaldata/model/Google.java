package com.townwizard.globaldata.model;

import java.util.List;

/**
 * Wrapper class for all Google specific classes (location, event, venue, etc)
 * 
 * This classes are populated from JSON returned from Google using reflection, that's why
 * there are no setters in them.
 */
public class Google {
    
    /**
     * Populated from loc JSON
     */
    public static class Loc {
        private Double lat;
        private Double lng;
    }    
    
    /**
     * Populated from geometry JSON
     */
    public static class Geometry {
        private Loc location;
    }
    
    /**
     * Populated from location JSON and can be converted into generic Location object
     */
    public static class Location implements Convertible <com.townwizard.globaldata.model.Location> {
        private String id;
        private String name;
        private Geometry geometry;
        @SuppressWarnings("unused") private String vicinity;
        private List<String> types;
        
        @Override
        public com.townwizard.globaldata.model.Location convert() {
            com.townwizard.globaldata.model.Location l = new com.townwizard.globaldata.model.Location();
            l.setSource(com.townwizard.globaldata.model.Location.Source.GOOGLE);
            l.setExternalId(id);
            l.setName(name);
            if(geometry != null) {             
                if(geometry.location != null) {
                    l.setLatitude(geometry.location.lat.floatValue());
                    l.setLongitude(geometry.location.lng.floatValue());
                }
            }
            if(types != null) {
                StringBuilder sb = new StringBuilder();
                for(String t : types) {
                    sb.append(t).append("|");
                }
                l.setCategoriesStr(sb.toString());
            }
            return l;
        }
    }
}