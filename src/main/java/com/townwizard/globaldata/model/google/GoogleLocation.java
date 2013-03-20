package com.townwizard.globaldata.model.google;

import java.util.List;

import com.townwizard.globaldata.model.Convertible;
import com.townwizard.globaldata.model.Location;

public class GoogleLocation implements Convertible <Location> {
    
    /** Do not rename fields **/
    private String id;
    private String name;
    private Geometry geometry;
    private String vicinity;
    private List<String> types;

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Geometry getGeometry() {
        return geometry;
    }
    public String getVicinity() {
        return vicinity;
    }
    public List<String> getTypes() {
        return types;
    }
    

    @Override
    public Location convert() {
        Location l = new Location();
        l.setSource(Location.Source.GOOGLE);
        l.setId(getId());
        l.setName(getName());
        if(getGeometry() != null) {             
            if(getGeometry().getLocation() != null) {
                l.setLatitude(getGeometry().getLocation().getLat().floatValue());
                l.setLongitude(getGeometry().getLocation().getLng().floatValue());
            }
        }
        if(getTypes() != null) {
            StringBuilder sb = new StringBuilder();
            for(String t : getTypes()) {
                sb.append(t).append("|");
            }
            l.setCategories(sb.toString());
        }
        return l;
    }

}
