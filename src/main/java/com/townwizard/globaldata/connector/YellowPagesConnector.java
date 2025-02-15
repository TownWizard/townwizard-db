package com.townwizard.globaldata.connector;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.http.client.ClientProtocolException;
import org.springframework.stereotype.Component;

import com.townwizard.db.constants.Constants;
import com.townwizard.db.util.HttpUtils;

/**
 * This class is responsible for execution of Yellow Pages HTTP queries
 */
@Component("yellowPagesConnector")
public final class YellowPagesConnector {
    
    private static final String SEARCH_URL = "http://api2.yp.com/listings/v1/search?";
    private static final int DEFAULT_LISTING_COUNT = 50;
    
    /**
     * Get locations (places) as JSON
     */
    public String executePlacesRequest(String term, String zip, double distanceInMiles,
            int pageNum, Integer listingCount)
            throws ClientProtocolException, IOException {
        StringBuilder sb = new StringBuilder(SEARCH_URL);
        sb.append("searchloc=").append(zip)
          .append("&term=").append(URLEncoder.encode(term, "UTF-8"))
          .append("&radius=").append(distanceInMiles)
          .append("&pagenum=").append(pageNum)
          .append("&listingcount=").append((listingCount != null) ? listingCount : DEFAULT_LISTING_COUNT);
        
        appendMandatoryParameters(sb);
        
        String url = sb.toString();
        String response = HttpUtils.executeGetRequest(url);
        return response;
    }
    
    private void appendMandatoryParameters(StringBuilder sb) {
        sb.append("&format=json")
          .append("&key=").append(Constants.YELLO_PAGES_API_KEY);
    }

}
