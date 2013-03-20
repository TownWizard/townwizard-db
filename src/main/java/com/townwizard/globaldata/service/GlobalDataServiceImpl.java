package com.townwizard.globaldata.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.logger.Log;
import com.townwizard.globaldata.model.DistanceComparator;
import com.townwizard.globaldata.model.Location;

@Component("globalDataService")
public class GlobalDataServiceImpl implements GlobalDataService {
    
    @Autowired
    private GoogleService googleService;
    @Autowired
    private YellowPagesService yellowPagesService;
    @Autowired
    private LocationService locationService;
    
    private ExecutorService executors = Executors.newFixedThreadPool(60);

    @Override
    public List<Location> getLocations(final String zip, String countryCode, final int distanceInMeters) {

        List<String> terms = getSearchTerms(zip, countryCode, distanceInMeters);
        List<Future<List<Location>>> results = new ArrayList<>(terms.size());
        
        for(final String term : terms) {
            Callable<List<Location>> worker = new Callable<List<Location>>() {
                @Override
                public List<Location> call() throws Exception {
                    return yellowPagesService.getLocations(term, zip, distanceInMeters);                    
                }                
            };            
            results.add(executors.submit(worker));
        }
        
        List<Location> finalList = new ArrayList<>();
        
        long start = System.currentTimeMillis();
        try {
            for(Future<List<Location>> r : results) {
                List<Location> ypLocationsForName = r.get();
                finalList.addAll(ypLocationsForName);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        long end = System.currentTimeMillis();
        if(Log.isDebugEnabled()) Log.debug(
                "Executed " + terms.size() + " requests and brought: " + 
                        finalList.size()  + " locations in " + (end - start) + " ms");        
        
        for(Location l : finalList) {
            l.setDistance(locationService.distance(l, zip, countryCode));
        }
        
        Collections.sort(finalList, new DistanceComparator());
        
        return finalList;
    }
    
    private List<String> getSearchTerms(String zip, String countryCode, int distanceInMeters) {
        List<Location> googleLocations = 
                googleService.getLocations(zip, countryCode, distanceInMeters);
        
        List<String> terms = new ArrayList<>(googleLocations.size());
        for(final Location gLocation : googleLocations) {
            terms.add(gLocation.getName());
        }
        
        return terms;
    }
    
}
