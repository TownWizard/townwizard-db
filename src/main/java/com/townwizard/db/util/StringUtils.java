package com.townwizard.db.util;

import java.util.HashSet;
import java.util.Set;

public final class StringUtils {

    private StringUtils() {}
    
    public static Set<String> split(String s, String separatorRegex) {
        Set<String> result = new HashSet<>();
        if(s != null) {
            String[] strings = s.split(separatorRegex);
            if(strings.length > 0) {
                for(String str : strings) {
                    if(str != null) {
                        String strTrimmed = str.trim();
                        if(!strTrimmed.isEmpty()) {
                            result.add(strTrimmed);
                        }
                    }
                }
            }
        }
        return result;
    }
}
