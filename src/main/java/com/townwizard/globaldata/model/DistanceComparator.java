package com.townwizard.globaldata.model;

import java.util.Comparator;

/**
 * Comparator which compares objects by their respective distances from some origin.
 * Object will be sorted by their distance from some origin.  If the distance is the same,
 * then objects are sorted by their names. 
 */
public class DistanceComparator  implements Comparator<DistanceComparable> {
 
    @Override            
    public int compare(DistanceComparable e1, DistanceComparable e2) {
        Integer d1 = e1.getDistance();
        Integer d2 = e2.getDistance();
        if(d1 != null && d2 != null) return d1.compareTo(d2);
        else if(d1 == null && d2 != null) return 1;
        else if(d1 != null && d2 == null) return -1;
        return compareNames(e1.getName(), e2.getName());
    }
    
    private int compareNames(String name1, String name2) {
        if(name1 == null) return 1;
        else if(name2 == null) return -1;
        return name1.compareTo(name2);
    }
    
}
