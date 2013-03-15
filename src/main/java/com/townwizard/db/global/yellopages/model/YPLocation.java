package com.townwizard.db.global.yellopages.model;

import com.townwizard.db.global.model.Convertible;
import com.townwizard.db.global.model.Location;

public class YPLocation implements Convertible <Location> {
    
    /** Do not rename fields **/
    private String businessName;

    public String getBusinessName() {
        return businessName;
    }

    @Override
    public Location convert() {
        Location l = new Location();
        l.setName(getBusinessName());
        return l;
    }
    
}
