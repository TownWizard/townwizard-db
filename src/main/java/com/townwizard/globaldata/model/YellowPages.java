package com.townwizard.globaldata.model;

/**
 * Wrapper class for all Yellow Pages specific classes (location, event, venue, etc)
 * 
 * This classes are populated from JSON returned from Yellow Pages using reflection, that's why
 * there are no setters in them.
 */
public class YellowPages {

    /**
     * Populated from Yellow Pages location JSON and can be converted into generic Location object
     */
    public static class Location implements Convertible <com.townwizard.globaldata.model.Location> {

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
        private String zip;

        @Override
        public com.townwizard.globaldata.model.Location convert() {
            com.townwizard.globaldata.model.Location l = new com.townwizard.globaldata.model.Location();
            l.setSource(com.townwizard.globaldata.model.Location.Source.YELLOW_PAGES);
            l.setExternalId(listingId.toString());        
            l.setName(businessName);
            l.setCategoriesStr(categories);
            l.setCity(city);
            if(latitude != null) l.setLatitude(latitude.floatValue());
            if(longitude != null) l.setLongitude(longitude.floatValue());
            l.setUrl(moreInfoURL);
            l.setPhone(phone);
            l.setCategory(primaryCategory);
            l.setState(state);
            l.setStreet(street);
            if(zip != null) l.setZip(zip.toString());
            return l;
        }
        
    }
}
