package com.townwizard.db.util;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

/**
 * String utilities.
 */
public final class StringUtils {

    private StringUtils() {}
    
    /**
     * Split a string, and return a Set of items in the split result (duplicates removed)
     */
    public static Set<String> split(String s, String separatorRegex) {
        return split(s, separatorRegex, false);
    }

    /**
     * Split a string, and return a Set of items in the split result (duplicates removed).
     * Optionally bring the items in the set to lower case.
     */    
    public static Set<String> split(String s, String separatorRegex, boolean toLower) {
        Set<String> result = new HashSet<>();
        if(s != null) {
            String[] strings = s.split(separatorRegex);
            if(strings.length > 0) {
                for(String str : strings) {
                    if(str != null) {
                        String strTrimmed = str.trim();
                        if(!strTrimmed.isEmpty()) {                            
                            result.add(toLower ? strTrimmed.toLowerCase() : strTrimmed);
                        }
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Return a string of 1s and 0s, which is a binary representation of a given decimal number. 
     */
    public static String toBinaryString(int value, int numDigits) {
        StringBuilder sb = new StringBuilder();
        for(int i = numDigits-1; i >= 0; i--) {
            sb.append(((value & (1 << i)) != 0) ? "1" :"0");
        }
        return sb.toString();
    }
    
    /**
     * Convert IP4 address, passed as a string to an integer resulting from concatenating
     * bytes representing parts of the IP. 
     */
    public static int ip4ToInteger(String ip) throws NumberFormatException {
        String[] parts = ip.split("\\.");
        StringBuilder sb = new StringBuilder();
        for(String part : parts) {
            int v = Integer.parseInt(part);
            sb.append(toBinaryString(v, 8));
        }
        int result = Integer.parseInt(sb.toString(), 2);
        return result;
    }
    
    /**
     * Base64 encode string
     */
    public static String base64Encode(String s) {
        return DatatypeConverter.printBase64Binary(s.getBytes()); 
    }
}
