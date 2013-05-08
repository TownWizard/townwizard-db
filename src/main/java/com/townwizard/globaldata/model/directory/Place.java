package com.townwizard.globaldata.model.directory;


import java.util.Date;
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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.townwizard.db.constants.Constants;
import com.townwizard.db.util.StringUtils;
import com.townwizard.globaldata.model.DistanceComparable;

/**
 * Represents a generic (that is not provider specific) location (place) object.
 * Location objects from different providers (YP, Google) may be converted to instances of this class.
 * The class imlements DistanceComparable so the instances of this class can be sorted by distance/name.
 * 
 * Instances of this class are saved in our local DB (so an object of this class is a Hibernate entity)
 */
@Entity
@Table(name = "Location")
public class Place implements DistanceComparable {
    
    /**
     * Enum representing possible location sources.  The ordinals of this enum
     * are used as location source valies in the DB
     */
    public static enum Source {
        NONE(0), YELLOW_PAGES(1), GOOGLE(2), FACEBOOK(3);
        
        private int id;
        private Source(int id) {this.id = id;}        
        public int getId() { return id; }
    }
    
    @Id @GeneratedValue @Column(nullable = false, updatable = false)
    private Long id;
    @JsonIgnore
    private Date created;
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
    private String category;           //location category (which is the only or primary category,
                                       //depending on the source
    @JsonIgnore @Transient
    private String categoriesStr;      //categories concatenated in pipe-separated string, not saved in DB
    @JsonIgnore
    @ManyToMany (mappedBy = "places", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    private Set<PlaceCategory> categories;
    @JsonIgnore
    @ManyToMany (mappedBy = "places", fetch=FetchType.LAZY)
    private Set<PlaceIngest> ingests;
    @Column(name="source")
    @Enumerated(EnumType.ORDINAL)
    private Source source;
    @Transient
    private Integer distance;          //calculated on our side on the fly, and not saved in the DB
    @Transient
    private Double distanceInMiles;    //calculated on our side on the fly, and not saved in the DB
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Date getCreated() {
        return created;
    }
    public void setCreated(Date created) {
        this.created = created;
    }    
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
    public Set<PlaceCategory> getCategories() {
        return categories;
    }
    public void setCategories(Set<PlaceCategory> categories) {
        this.categories = categories;
    }
    public Set<PlaceIngest> getIngests() {
        return ingests;
    }
    public void setIngests(Set<PlaceIngest> ingests) {
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
    
    /**
     * Parse category and categoriesStr values into a set of categories.
     * This is used to populate categories in the DB
     */
    @JsonIgnore
    public Set<String> extractCategoryNames() {
        Set<String> cats = StringUtils.split(categoriesStr, "\\|");
        cats.addAll(StringUtils.split(category, "\\|"));
        return cats;
    }
    
    /**
     * Converts categories (as list of LocationCategory objects) to list of strings,
     * which only contain category names.
     */
    @JsonIgnore
    public List<String> getCategoryNames() {
        List<String> result = new LinkedList<>();
        if(categories != null) {
            for(PlaceCategory c : categories) {
                result.add(c.getName());
            }
        }
        return result;
    }
    
    /**
     * Convenience method to add location category to the location, which will set both sides of
     * the Location <-> LocationCategory relationships
     */
    public void addCategory(PlaceCategory c) {
        if(categories == null) {
            categories = new HashSet<>();
        }
        categories.add(c);
        c.addPlace(this);
    }

    /**
     * Convenience method to add location ingest to the location, which will set both sides of
     * the Location <-> LocationIngest relationships
     */
    public void addIngest(PlaceIngest i) {
        if(ingests == null) {
            ingests = new HashSet<>();
        }
        ingests.add(i);
        i.addPlace(this);
    }    
    
    @Override
    public String toString() {
        return "[" + latitude + "," + longitude + "," + zip + "," + countryCode + "] - " + city;
    }

}
