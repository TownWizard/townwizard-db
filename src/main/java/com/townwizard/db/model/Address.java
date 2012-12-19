package com.townwizard.db.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A model class for address objects.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="users")
public class Address extends AuditableEntity {
    
    private static final long serialVersionUID = -7937478988122266498L;
    
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    @OneToOne @JoinColumn(name = "userId")
    private User user;
    
    public String getAddress1() {
        return address1;
    }
    public void setAddress1(String address1) {
        this.address1 = address1;
    }
    public String getAddress2() {
        return address2;
    }
    public void setAddress2(String address2) {
        this.address2 = address2;
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
    public String getPostalCode() {
        return postalCode;
    }
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    
    @JsonIgnore
    public User getUser() {
        return user;
    }
    @JsonIgnore
    public void setUser(User user) {
        this.user = user;
    }
    
    @JsonIgnore
    public boolean isValid() {
       return address1 != null && city != null && state != null && postalCode != null;
    }
    
    @Override
    public String toString() {
        return "Address [address1=" + address1 + ", address2=" + address2
                + ", city=" + city + ", state=" + state + ", postalCode="
                + postalCode + ", country=" + country + "]";
    }

}