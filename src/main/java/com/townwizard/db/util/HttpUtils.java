package com.townwizard.db.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

/**
 * HTTP client helper methods
 */
public final class HttpUtils {
    
    private HttpUtils(){}
    
    /**
     * Execute get request and return the response as a string.
     */
    public static String executeGetRequest(String path) throws IOException, ClientProtocolException {
        return executeGetRequest(path, null);
    }
    
    /**
     * Execute get request with optional headers and return the response as a string.
     */
    public static String executeGetRequest(String path, Map<String, String> headers)
            throws IOException, ClientProtocolException {        
        HttpGet get = new HttpGet(path);
        setRequestHeaders(get, headers);
        HttpResponse response = getHttpClient().execute(get);
        return copyToString(response.getEntity().getContent());        
    }    
    
    /**
     * Execute post request and return the response as a string.
     */
    public static String executePostRequest(String path, String entity, Map<String, String> headers)
            throws IOException {
        HttpPost post = new HttpPost(path);
        setRequestHeaders(post, headers);
        post.setEntity(new StringEntity(entity));
        HttpResponse response = getHttpClient().execute(post);
        return copyToString(response.getEntity().getContent());
    }
    
    private static HttpClient getHttpClient() {
        HttpClient c = new DefaultHttpClient();
        c.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
        c.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
        return c;
    }
    
    private static void setRequestHeaders(HttpRequestBase request, Map<String, String> headers) {
        if(headers != null) {
            for(Map.Entry<String, String> e : headers.entrySet()) {
                request.addHeader(e.getKey(), e.getValue());
            }
        }
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
