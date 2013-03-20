package com.townwizard.globaldata.model.yellopages;

import com.townwizard.globaldata.model.Convertible;
import com.townwizard.globaldata.model.Location;

public class YPLocation implements Convertible <Location> {
    
    /** Do not rename fields **/
    private Integer listingId;
    private String businessName;
    private String categories; //pipe-separated string
    private String city;
    private Double latitude;
    private Double longitude;
    private String moreInfoURL;
    private String phone;
    private String primaryCategory;
    private String state;
    private String street;
    private Integer zip;

    public String getBusinessName() {
        return businessName;
    }
    public Integer getListingId() {
        return listingId;
    }
    public String getCategories() {
        return categories;
    }
    public String getCity() {
        return city;
    }
    public Double getLatitude() {
        return latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public String getMoreInfoURL() {
        return moreInfoURL;
    }
    public String getPhone() {
        return phone;
    }
    public String getPrimaryCategory() {
        return primaryCategory;
    }
    public String getState() {
        return state;
    }
    public String getStreet() {
        return street;
    }
    public Integer getZip() {
        return zip;
    }

    @Override
    public Location convert() {
        Location l = new Location();
        l.setSource(Location.Source.YELLOW_PAGES);
        l.setId(getListingId().toString());        
        l.setName(getBusinessName());
        l.setCategories(getCategories());
        l.setCity(getCity());
        l.setLatitude(getLatitude().floatValue());
        l.setLongitude(getLongitude().floatValue());
        l.setUrl(getMoreInfoURL());
        l.setPhone(getPhone());
        l.setCategory(getPrimaryCategory());
        l.setState(getState());
        l.setStreet(getStreet());
        Integer zip = getZip();
        if(zip != null) {
            l.setZip(getZip().toString());
        }
        return l;
    }
    
}
