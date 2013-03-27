package com.townwizard.globaldata.model;

public class CityLocation {

    private String city;
    private String postalCode;
    private String countryCode;
    private Double latitude;
    private Double longitude;
    
    public CityLocation(String city, String postalCode, String countryCode, Double latitude, Double longitude) {
        this.city = city;
        this.postalCode = postalCode;
        this.countryCode = countryCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getCity() {
        return city;
    }
    
    public String getPostalCode() {
        return postalCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
    
    public boolean hasLocation() {
        return latitude != null && longitude != null && latitude != 0 && longitude != 0;
    }
    
    public boolean hasPostalCodeAndCountry() {
        return postalCode != null && countryCode != null;
    }
   
}
