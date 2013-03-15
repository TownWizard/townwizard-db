package com.townwizard.db.global.google.model;

import com.townwizard.db.global.model.Convertible;
import com.townwizard.db.global.model.Location;

public class GoogleLocation implements Convertible <Location> {
    
    private String name;

    public String getName() {
        return name;
    }

    @Override
    public Location convert() {
        Location l = new Location();
        l.setName(getName());
        return l;
    }

}
