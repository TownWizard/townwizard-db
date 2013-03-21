package com.townwizard.globaldata.model;

import java.util.List;

public class Google {
    
    public static class Loc {
        private Double lat;
        private Double lng;
    }    
    
    public static class Geometry {
        private Loc location;
    }
    
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