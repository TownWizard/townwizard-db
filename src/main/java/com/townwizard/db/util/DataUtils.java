package com.townwizard.db.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Utilities for working with data files.
 */
public final class DataUtils {
    
    private DataUtils() {}
    
    /**
     * Load CSV file to a Java map. The keys of this map will be of type K, and the values
     * will be lists of type O.  In case when the keys are unique, the value lists will all have
     * size 0 or 1.
     * 
     * Example:
     * 
     * DataUtils.csvToMap(
                getDataInputStream("myCsvFile"), //input file stream 
                1,                               //lines to skip
                new int[]{1, 0, 2, 4, 9, 10},    //column indexes
                new String[] {"zip", "countryCode", "city", "state", "latitude", "longitude"}, //Java bean field names
                String.class,                    //key class                
                Location.class,                  //value class
                "\t",                            //separator (comma in CSV files, but may be enything else)
                "");                             //encloser
     * 
     * @param dataStream      The file input stream
     * @param skipLines       Number of lines to skip
     * @param columns         An array of integer column indexes. The items in the first column index
     *                        in this array will be used as map keys
     * @param fieldNames      An array of Java object filed names. The method will use reflection
     *                        to populate Java object filds by these field names.
     *                        The order of the fields should match the order of column indexes.
     * @param keyClass        Java class used to instantiate map keys.
     * @param objectClass     Java class used to instantiate map values.
     * @param separator       String, separating data items in the file
     * @param encloser        String, enclosing data items in the file. It is ignored, if some columns
     *                        don't use the encloser.
     *                        
     * @return                Map which points keys to the lists of objects
     * 
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchFieldException
     */
    public static <K,O> Map<K, List<O>> dataFileToMap(InputStream dataStream, int skipLines,
            int[] columns, String[] fieldNames, Class<K> keyClass, Class<O> objectClass,
            String separator, String encloser) 
            throws IOException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        
        BufferedReader in = null;
        Map<K,List<O>> map = new HashMap<>();
        try {        
            in = new BufferedReader(new InputStreamReader(dataStream));
            String line;
            int count = 0;
            while((line = in.readLine()) != null) {
                if(count++ < skipLines) continue;

                String[] allValues = line.split(separator);

                String[] values = new String[columns.length];
                for(int i = 0; i < columns.length; i++) {
                    int index = columns[i];
                    if(allValues.length <= index) continue;
                    String field = allValues[index];
                    if(field.startsWith(encloser)) {
                        field = field.substring(encloser.length(), field.length()-encloser.length()).trim(); 
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
                        List<O> entries = map.get(key);
                        if(entries == null) {
                            entries = new LinkedList<>();
                            map.put(key, entries);
                        }
                        entries.add(o);
                    }
                }
            }
        } finally {
           if(in != null) in.close();
        }

        return map;
    }

}
