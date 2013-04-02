package com.townwizard.globaldata.model;

/**
 * Objects of classes implementing this interface can be used to calculate their distance
 * from some origin location.  Location and Event class are both distance comparable
 */
public interface DistanceComparable {

    String getName();
    Integer getDistance();
    
}