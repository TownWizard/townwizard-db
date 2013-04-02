package com.townwizard.globaldata.model;

/**
 * This interface is used for conversion between vendor-specific global items (events, locations)
 * and generic ones.
 */
public interface Convertible<T> {
    
    /**
     * Convert myself to some object of type T
     */
    T convert();

}
