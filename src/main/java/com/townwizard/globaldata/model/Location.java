package com.townwizard.globaldata.model;


import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.townwizard.db.constants.Constants;
import com.townwizard.db.model.AbstractEntity;
import com.townwizard.db.util.StringUtils;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="locations")
public class Location extends AbstractEntity implements DistanceComparable {
    
    private static final long serialVersionUID = 3832190748475773728L;

    public static enum Source {
        NONE(0), YELLOW_PAGES(1), GOOGLE(2), FACEBOOK(3);
        
        private int id;
        private Source(int id) {this.id = id;}        
        public int getId() { return id; }
    }
    
    private String externalId;
    private String name;
    private String zip;
    private String city;
    private String state;
    private String countryCode;
    private Float latitude;
    private Float longitude;
    private String url;
    private String phone;
    private String street;
    private String category;
    @JsonIgnore @Transient
    private String categoriesStr;
    @JsonIgnore
    @ManyToMany (mappedBy = "locations", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
    private Set<LocationCategory> categories;
    @JsonIgnore
    @ManyToMany (mappedBy = "locations", fetch=FetchType.LAZY)
    private Set<LocationIngest> ingests;
    @Column(name="source")
    @Enumerated(EnumType.ORDINAL)
    private Source source;
    @Transient
    private Integer distance;
    @Transient
    private Double distanceInMiles;
    
    public String getExternalId() {
        return externalId;
    }
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
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
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
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
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getStreet() {
        return street;
    }
    public void setStreet(String street) {
        this.street = street;
    }
    public String getCategoriesStr() {
        return categoriesStr;
    }
    public void setCategoriesStr(String categoriesStr) {
        this.categoriesStr = categoriesStr;
    }
    public Set<LocationCategory> getCategories() {
        return categories;
    }
    public void setCategories(Set<LocationCategory> categories) {
        this.categories = categories;
    }
    public Set<LocationIngest> getIngests() {
        return ingests;
    }
    public void setIngests(Set<LocationIngest> ingests) {
        this.ingests = ingests;
    }
    public Source getSource() {
        return source;
    }
    public void setSource(Source source) {
        this.source = source;
    }
    public Integer getDistance() {
        return distance;
    }
    public void setDistance(Integer distance) {
        this.distance = distance;
        this.distanceInMiles = distance / Constants.METERS_IN_MILE;
    }
    public Double getDistanceInMiles() {
        return distanceInMiles;
    }
    public void setDistanceInMiles(Double distanceInMiles) {
        this.distanceInMiles = distanceInMiles;
        this.distance = new Double(distanceInMiles * Constants.METERS_IN_MILE).intValue();
    }
    
    @JsonIgnore
    public Set<String> extractCategoryNames() {
        Set<String> cats = StringUtils.split(categoriesStr, "\\|");
        cats.addAll(StringUtils.split(category, "\\|"));
        return cats;
    }
    
    public List<String> getCategoryNames() {
        List<String> result = new LinkedList<>();
        if(categories != null) {
            for(LocationCategory c : categories) {
                result.add(c.getName());
            }
        }
        return result;
    }
    
    public void addCategory(LocationCategory c) {
        if(categories == null) {
            categories = new HashSet<>();
        }
        categories.add(c);
        c.addLocation(this);
    }
    
    public void addIngest(LocationIngest i) {
        if(ingests == null) {
            ingests = new HashSet<>();
        }
        ingests.add(i);
        i.addLocation(this);
    }    
    
    @Override
    public String toString() {
        return "[" + latitude + "," + longitude + "," + zip + "," + countryCode + "] - " + city;
    }

}
