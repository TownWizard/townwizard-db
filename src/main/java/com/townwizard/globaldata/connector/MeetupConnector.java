package com.townwizard.globaldata.connector;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

import com.townwizard.db.util.HttpUtils;

public final class MeetupConnector {
    
    private static final String OPEN_EVENTS_URL = "https://api.meetup.com/2/open_events?";
    
    public String executeEventsRequest(Map<String, String> params) 
            throws ClientProtocolException, IOException {
        return HttpUtils.executeGetRequest(appendParams(OPEN_EVENTS_URL, params));
    }
    
    private String appendParams(String url, Map<String, String> params) {
        StringBuilder sb = new StringBuilder(url);
        boolean first = true;
        for(Map.Entry<String, String> e : params.entrySet()) {
            if(!first) sb.append("&");
            sb.append(e.getKey()).append("=").append(e.getValue());
            if(first) first = false;
        }
        return sb.toString();
    }    
    
}
