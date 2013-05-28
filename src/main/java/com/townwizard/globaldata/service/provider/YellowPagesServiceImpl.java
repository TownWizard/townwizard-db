package com.townwizard.globaldata.service.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.sf.ehcache.util.NamedThreadFactory;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.constants.Constants;
import com.townwizard.db.logger.Log;
import com.townwizard.db.util.JSONUtils;
import com.townwizard.globaldata.connector.YellowPagesConnector;
import com.townwizard.globaldata.model.YellowPages;
import com.townwizard.globaldata.model.directory.Place;

/**
 * Yellow Pages service implementation
 */
@Component("yellowPagesService")
public class YellowPagesServiceImpl implements YellowPagesService {
    
    private static final int NUM_THREADS = 5;
    private static final int NUM_PLACES_IN_RESPONSE = 50;
    private static final int MAX_PAGES = NUM_THREADS * 3;
    
    @Autowired
    private YellowPagesConnector connector;
    
    @Override
    public List<Place> getPageOfPlaces(String zip, String term, int pageNum, int listingCount) {
        try {
            String json = connector.executePlacesRequest(
                    term, zip, Constants.PLACE_DISTANCE_IN_MILES, pageNum, listingCount);
            return jsonToPlaces(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes one HTTP request to get locations
     */
    @Override
    public List<Place> getPlaces(String zip, String term) throws Exception {
        List<Place> finalResult = new ArrayList<>();
        Exception ex = null;
        
        for(int attempt = 1; attempt <= 3; attempt++) {
            try {
                List<Place> result = null;
                int pages = 0;
                do {
                    result = getPagesOfPlaces(zip, Constants.PLACE_DISTANCE_IN_MILES, term, pages);
                    finalResult.addAll(result);
                    pages += NUM_THREADS;
                } while(pages < MAX_PAGES  && result.size() >= NUM_THREADS * NUM_PLACES_IN_RESPONSE);
                
                if(attempt > 1) {
                    Log.warning("Successfully retrieved places for zip '" + zip + 
                            "' and term '" + term  + "'" + " after attempt " + attempt);
                }
                
                return finalResult;
            } catch (TimeoutException e) {
                Log.warning("Exception happend while getting YP places for zip " + zip + 
                        " and term '" + term + "' " + "on attempt " + attempt);
                ex = e;
            } catch (Exception e) {
                throw e;
            }
        }

        Log.warning("Could not get YP places for zip " + zip + " and term '" + term + "'");
        throw new RuntimeException(ex);
    }
    
    private List<Place> getPagesOfPlaces(final String zip, final double distanceInMiles, final String term,
            final int index) 
        throws TimeoutException, ExecutionException, InterruptedException {

        ExecutorService httpExecutors = Executors.newFixedThreadPool(
                NUM_THREADS, new NamedThreadFactory("yp-http-executor"));
        
        class Executor implements Callable<List<Place>> {
            private int pageNum;
            Executor(int pageNum){
                this.pageNum = pageNum;
            }

            @Override
            public List<Place> call() throws Exception {
                String json = connector.executePlacesRequest(term, zip, distanceInMiles, pageNum, null);
                return jsonToPlaces(json);
            }            
        }
        
        List<Future<List<Place>>> futures = new ArrayList<>(NUM_THREADS);
        for(int i = index+1; i <= index + NUM_THREADS; i++) {
            futures.add(httpExecutors.submit(new Executor(i)));
        }
        httpExecutors.shutdown();
        
        List<Place> result = new ArrayList<>();
        for(Future<List<Place>> f : futures) {
            List<Place> places = f.get(60, TimeUnit.SECONDS);
            result.addAll(places);
        }
        return result;
    }
    
    private <T> List<T> jsonToObjects (String json, Class<T> objectClass) 
            throws JSONException, IllegalAccessException, InstantiationException {        
        JSONObject j = new JSONObject(json);
        JSONObject searchResult = j.optJSONObject("searchResult");
        if(searchResult != null) {
            JSONObject searchListings = searchResult.optJSONObject("searchListings");
            if(searchListings != null) { 
                JSONArray data = searchListings.getJSONArray("searchListing");
                return JSONUtils.jsonToObjects(data, objectClass);
            }
        }
        return Collections.emptyList();
    }
    
    private List<Place> jsonToPlaces(String json) throws Exception {
        List<YellowPages.Location> gObjects = jsonToObjects(json, YellowPages.Location.class);
        return ServiceUtils.convertList(gObjects);        
    }

}
