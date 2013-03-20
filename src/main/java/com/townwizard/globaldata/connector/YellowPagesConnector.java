package com.townwizard.globaldata.connector;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.http.client.ClientProtocolException;
import org.springframework.stereotype.Component;

import com.townwizard.db.constants.Constants;
import com.townwizard.db.logger.Log;
import com.townwizard.db.util.HttpUtils;

@Component("yellowPagesConnector")
public final class YellowPagesConnector {
    
    private static final String SEARCH_URL = "http://api2.yp.com/listings/v1/search?";
    
    public String executePlacesRequest(String term, String zip, double distanceInMiles)
            throws ClientProtocolException, IOException {
        
        StringBuilder sb = new StringBuilder(SEARCH_URL);
        sb.append("searchloc=").append(zip)
          .append("&term=").append(URLEncoder.encode(term, "UTF-8"))
          .append("&radius=").append(distanceInMiles);
        
        appendMandatoryParameters(sb);
        
        String url = sb.toString();
        Log.debug(url);
        String response = HttpUtils.executeGetRequest(url);
        return response;
    }
    
    private void appendMandatoryParameters(StringBuilder sb) {
        sb.append("&sort=distance")
          .append("&listingcount=10")
          .append("&format=json")
          .append("&key=").append(Constants.YELLO_PAGES_API_KEY);
    }

}
