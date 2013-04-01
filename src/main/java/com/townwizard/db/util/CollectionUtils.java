package com.townwizard.db.util;

import java.util.Iterator;
import java.util.List;

/**
 * Collection utils
 */
public final class CollectionUtils {
    
    private CollectionUtils() {}

    /**
     * Return a comma separated string, 
     * which is the result of concatenation of the list items 
     */
    public static String join(List<?> list) {
        return join(list, null, null);
    }
    
    /**
     * Concatenate list items in a string.  Append separator between items,
     * and enclose each item in the encloser.
     * If separator is null, comma is used.
     * If encloser is null, an empty string is used. 
     */
    public static String join(List<?> list, String separator, String encloser) {        
        if(list == null || list.isEmpty()) return "";
        String sep = separator != null ? separator : ",";
        String enc = encloser != null ? encloser : "";
        
        StringBuilder sb = new StringBuilder();
        Iterator<?> i = list.iterator();
        while(i.hasNext()) {
            sb.append(enc).append(i.next()).append(enc);
            if(i.hasNext()) sb.append(sep);
        }
        return sb.toString();
    }

}
