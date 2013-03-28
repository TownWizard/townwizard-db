package com.townwizard.db.util;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public final class ReflectionUtils {
    
    private ReflectionUtils() {}
    
    public static <O> O createAndPopulate(Class<O> objectClass, String[] fieldNames, String[] values) 
        throws IllegalAccessException, InstantiationException, NoSuchFieldException {
        
        O o = objectClass.newInstance();
        for(int i = 0; i < fieldNames.length; i ++) {
            Field f = objectClass.getDeclaredField(fieldNames[i]);
            f.setAccessible(true);
            Type t = f.getGenericType();
            String valueStr = values[i];
            if(valueStr != null && valueStr.length() > 0) {
                if(t.equals(String.class)) {
                    f.set(o, valueStr);    
                } else if(t.equals(Integer.class)) {
                    f.set(o, new Integer(valueStr));
                } else if(t.equals(Float.class)) {                
                    f.set(o, new Float(valueStr));                
                } else if(t.equals(Long.class)) {
                    f.set(o, new Long(valueStr));
                } else if(t.equals(Double.class)) {
                    f.set(o, new Double(valueStr));
                }
            }
        }
        return o;
    }
    
    public static void populateFromJson(Object o, JSONObject j) {
        try {
            Field[] fields = o.getClass().getDeclaredFields();
            for (Field f : fields) {
                Object value = j.opt(f.getName());
                if (value != null) {
                    setField(f, o, value);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    private static void setField(Field f, Object target, Object value)
            throws IllegalAccessException, InstantiationException {
        f.setAccessible(true);
        Object v = null;
        Type type = f.getGenericType();
        Class<?> valueClass = value.getClass();
        if(type.equals(String.class)) {
            String val = value.toString();
            if(!"null".equals(val)) v = val;
        } else if(type.equals(valueClass)) {
            v = value;
        } else if(type.equals(Double.class) && valueClass == Integer.class) {
            v = new Double(((Integer)value).intValue());
        } else if(type.equals(Float.class) && valueClass == Integer.class) {
            v = new Float(((Integer)value).intValue());
        } else if(value instanceof JSONArray) { //only JSON arrays of strings supported
            JSONArray valueArr = (JSONArray)value;
            if(valueArr.length() >0) {            
                v = new ArrayList<String>();
                @SuppressWarnings("unchecked")
                List<String> l = (List<String>)v;
                for (int i = 0; i < valueArr.length(); i++) {
                    String s = valueArr.optString(i);
                    if(s != null) {
                        l.add(s);
                    }
                }
            }
        } else if(value instanceof JSONObject) {
            v = ((Class<?>)type).newInstance();
            populateFromJson(v, (JSONObject)value);            
        }
        f.set(target, v);
    }
}