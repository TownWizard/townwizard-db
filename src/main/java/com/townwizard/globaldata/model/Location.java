package com.townwizard.globaldata.model;

/**
 * This class represents a location object which can be defined by providing either of:
 * 
 * 1) Zip info (zip code and country code)
 * 2) Latitude and longitude
 * 3) IP address
 */
public class Location {
    
    private String zip;
    private String city;
    private String countryCode;
    private Float latitude;
    private Float longitude;
    private String ip;

    public Location(){}
    
    /**
     * Create an object from latitude and longitude
     */
    public Location(Float latitude, Float longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Create an object from all parameters, which are optional.
     * Here, location parameter is a string containing comma-separated latitude and longitude
     */
    public Location(String zip, String countryCode, String location, String ip) {
        this.zip = zip;
        this.countryCode = countryCode;
        this.ip = ip;
        setLatitudeAndLongitude(location);
    }    
    
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
    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    public boolean isLocationSet() {
        return latitude != 0 && longitude != 0;
    }

    public boolean isZipInfoSet() {
        return zip != null && !zip.isEmpty() && countryCode != null && !countryCode.isEmpty();
    }
    
    public boolean isIpSet() {
        return ip != null && !ip.isEmpty();
    }    

    @Override
    public String toString() {
        return "[" + latitude + "," + longitude + "," + zip + "," + countryCode + "] - " + city;
    }
    
    private void setLatitudeAndLongitude(String location) {
        if(location != null) {
            String[] latAndLon = location.split(",");
            if(latAndLon.length == 2) {
                try {
                    latitude = Float.parseFloat(latAndLon[0]);
                    longitude = Float.parseFloat(latAndLon[1]);                         
                } catch (NumberFormatException e) {
                    latitude = longitude = 0f;
                }
            }
        }
    }

}
