package com.townwizard.db.logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;

public class Formatter extends java.util.logging.Formatter {
    
    private static final DateFormat dateFormat = new SimpleDateFormat("kk:mm:ss,S");
    
    @Override
    public String format(LogRecord record) {
        Date d = new Date(record.getMillis());
        
        String source;
        if (record.getSourceClassName() != null) {
            source = record.getSourceClassName();
            if (record.getSourceMethodName() != null) {
               source += " " + record.getSourceMethodName();
            }
        } else {
            source = record.getLoggerName();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(dateFormat.format(d))
            .append(" ").append(record.getLevel())
            .append(" ").append(source)
            .append(" - ").append(record.getMessage())
            .append("\n");
        
        return sb.toString();
    }
    
}
