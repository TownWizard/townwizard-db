package com.townwizard.db.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

/**
 * HTTP client helper methods
 */
public final class HttpUtils {
    
    private HttpUtils(){}
    
    /**
     * Execute get request, and return the response as a string.
     */
    public static String executeGetRequest(String path) throws IOException, ClientProtocolException {        
        HttpClient c = new DefaultHttpClient();
        c.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
        c.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
        HttpGet get = new HttpGet(path);
        HttpResponse response = c.execute(get);
        String result = copyToString(response.getEntity().getContent());
        return result;
    }
    
    private static String copyToString(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringWriter out = new StringWriter();
        String s;
        while((s = in.readLine()) != null) {
            out.append(s);
        }
        return out.toString();
    }

}
