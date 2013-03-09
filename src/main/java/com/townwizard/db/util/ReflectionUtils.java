package com.townwizard.db.util;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

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
            if(valueStr.length() > 0) {
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
    
    public static String toHtml(Object o) {
        return toHtml(o, 0);
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

    private static String toHtml(Object o, int indent) {
        StringBuilder sb = new StringBuilder();
        
        try {            
            boolean empty = true;
            Field[] fields = o.getClass().getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
                Object value = f.get(o);
                if(value != null) {
                    if(empty) {
                        empty = false;
                        sb.append("<div style=\"padding-bottom:10;padding-left:").append(indent*50).append("\">");
                    }
                    if(isAtomic(f)) {
                        sb.append("<span>").append(f.getName()).append(":&nbsp;")
                          .append(value).append("</span><br/>");
                    } else {
                        sb.append(f.getName()).append(":&nbsp;").append(toHtml(value, indent+1));
                    }
                }
            }
            if(!empty) {
                sb.append("</div>");
            }
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
        
        return sb.toString();        
    }
    
    private static void setField(Field f, Object target, Object value)
            throws IllegalAccessException, InstantiationException {
        f.setAccessible(true);
        Object v = null;
        Type type = f.getGenericType();
        if(type.equals(String.class)) {
            v = value.toString();
        } else if(isAtomic(f)){
            v = value;
        } else {
            v = ((Class<?>)type).newInstance();
            if(value instanceof JSONObject) {
                populateFromJson(v, (JSONObject)value);
            }
        }
        f.set(target, v);
    }
    
    private static boolean isAtomic(Field f) {
        Type type = f.getGenericType();
        return type.equals(String.class) ||
               type.equals(Long.class) ||
               type.equals(Integer.class) ||
               type.equals(Float.class) ||
               type.equals(Double.class);
    }
}