package com.townwizard.db.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class with date handling helper methods
 */
public final class DateUtils {

    private DateUtils(){}
    
    /**
     * Get a date representing beginning of a day
     */
    public static Date floor(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * Get a date representing the end of the day (last millisecond of the day)
     */
    public static Date ceiling(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }
    
    /**
     * Add days to a date 
     */
    public static Date addDays(Date date, int days){
        Calendar c = Calendar.getInstance();
        c.setTime(date);        
        c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + days);
        return c.getTime();
    }
    
    /**
     * Return the current time in the given time zone
     */
    public static Date now(TimeZone timeZone) {  
        long otherOffset = timeZone.getRawOffset();
        long myOffset = Calendar.getInstance().getTimeZone().getRawOffset();
        long offsetDiff = myOffset - otherOffset;
        long time = System.currentTimeMillis() - offsetDiff;
        return new Date(time);
    }    
}
