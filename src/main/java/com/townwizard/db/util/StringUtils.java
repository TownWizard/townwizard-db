package com.townwizard.db.util;

import java.util.HashSet;
import java.util.Set;

public final class StringUtils {

    private StringUtils() {}
    
    public static Set<String> split(String s, String separatorRegex) {
        return split(s, separatorRegex, false);
    }
    
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
    
    public static String toBinaryString(int value, int numDigits) {
        StringBuilder sb = new StringBuilder();
        for(int i = numDigits-1; i >= 0; i--) {
            sb.append(((value & (1 << i)) != 0) ? "1" :"0");
        }
        return sb.toString();
    }
    
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
}
