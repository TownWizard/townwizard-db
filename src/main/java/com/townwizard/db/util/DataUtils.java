package com.townwizard.db.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public final class DataUtils {
    
    private DataUtils() {}
    
    public static <K,O> Map<K, O> csvToMap(InputStream csvFile, int skipLines,
            int[] columns, String[] fieldNames, Class<K> keyClass, Class<O> objectClass) 
            throws IOException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        
        BufferedReader in = null;
        Map<K,O> map = new HashMap<>();
        try {        
            in = new BufferedReader(new InputStreamReader(csvFile));
            String line;
            int count = 0;
            while((line = in.readLine()) != null) {
                if(count++ < skipLines) continue;

                String[] allValues = line.split(",");

                String[] values = new String[columns.length];
                for(int i = 0; i < columns.length; i++) {
                    String field = allValues[columns[i]];
                    if(field.startsWith("\"")) {
                        field = field.substring(1, field.length()-1).trim(); 
                    }
                    values[i] = field;
                }

                String keyObjectStr = values[0];
                if(keyObjectStr.length() > 0) {
                    K key = null;
                    if(keyClass == String.class){                    
                        @SuppressWarnings("unchecked")
                        K k = (K)keyObjectStr;
                        key = k;
                    } else if (keyClass == Integer.class) {
                        @SuppressWarnings("unchecked")
                        K k = (K)new Integer(keyObjectStr);
                        key = k;
                    } else if (keyClass == Long.class) {
                        @SuppressWarnings("unchecked")
                        K k = (K) new Long(keyObjectStr);
                        key = k;
                    }
                    if(key != null) {
                        O o = ReflectionUtils.createAndPopulate(objectClass, fieldNames, values);
                        map.put(key, o);
                    }
                }
            }
        } finally {
           if(in != null) in.close();
        }

        return map;
    }

}
