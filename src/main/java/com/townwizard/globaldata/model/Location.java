package com.townwizard.globaldata.model;

public class Location {
    
    private String zip;
    private String city;
    private String countryCode;
    private Float latitude;
    private Float longitude;
    
    public String getZip() {
        return zip;
    }
    public void setZip(String zip) {
        this.zip = zip;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getCountryCode() {
        return countryCode;
    }
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    public Float getLatitude() {
        return latitude;
    }
    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }
    public Float getLongitude() {
        return longitude;
    }
    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "[" + latitude + "," + longitude + "," + zip + "," + countryCode + "] - " + city;
    }

}
