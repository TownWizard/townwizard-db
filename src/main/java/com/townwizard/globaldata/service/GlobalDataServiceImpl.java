package com.townwizard.globaldata.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.constants.Constants;
import com.townwizard.db.global.google.service.GoogleService;
import com.townwizard.db.global.yellopages.service.YellowPagesService;
import com.townwizard.db.logger.Log;
import com.townwizard.globaldata.model.Location;

@Component("globalDataService")
public class GlobalDataServiceImpl implements GlobalDataService {
    
    @Autowired
    private GoogleService googleService;
    @Autowired
    private YellowPagesService yellowPagesService;
    
    private ExecutorService executors = Executors.newFixedThreadPool(60);

    @Override
    public List<Location> getLocations(final String zip, final int distanceInMeters) {
        //return facebookService.getLocations(zip, DEFAULT_COUNTRY_CODE, DEFAULT_DISTANCE_IN_METERS);
        List<Location> googleLocations = 
                googleService.getLocations(zip, Constants.DEFAULT_COUNTRY_CODE, distanceInMeters);
        
        List<Location> finalList = new ArrayList<>();
        finalList.addAll(googleLocations);
        
                
        List<Future<List<Location>>> results = new ArrayList<>(googleLocations.size());
        
        for(final Location gLocation : googleLocations) {
            Callable<List<Location>> worker = new Callable<List<Location>>() {
                @Override
                public List<Location> call() throws Exception {
                    return yellowPagesService.getLocations(gLocation.getName(), zip, distanceInMeters);                    
                }                
            };            
            results.add(executors.submit(worker));
        }
        
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
                "Executed " + googleLocations.size() + " requests and brought: " + 
                        finalList.size()  + " locations in " + (end - start) + " ms");
        
        return finalList;
    }
    
}
