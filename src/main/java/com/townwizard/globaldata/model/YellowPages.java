package com.townwizard.globaldata.model;

public class YellowPages {

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
        private Integer zip;

        @Override
        public com.townwizard.globaldata.model.Location convert() {
            com.townwizard.globaldata.model.Location l = new com.townwizard.globaldata.model.Location();
            l.setSource(com.townwizard.globaldata.model.Location.Source.YELLOW_PAGES);
            l.setId(listingId.toString());        
            l.setName(businessName);
            l.setCategories(categories);
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
