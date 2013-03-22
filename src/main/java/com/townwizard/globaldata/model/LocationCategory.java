package com.townwizard.globaldata.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="locations")
public class LocationCategory implements Serializable {

    private static final long serialVersionUID = -6790493186121169497L;

    @Id @GeneratedValue @Column(nullable = false, updatable = false)
    private Long id;
    private String name;
    
    @ManyToMany (fetch=FetchType.EAGER, cascade=CascadeType.ALL)    
    @JoinTable (
            name = "Location_LocationCategory",
            joinColumns= {@JoinColumn (name="location_category_id")},
            inverseJoinColumns = {@JoinColumn(name="location_id")}
    )    
    private Set<Location> locations;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Set<Location> getLocations() {
        return locations;
    }
    public void setLocations(Set<Location> locations) {
        this.locations = locations;
    }
    
    public void addLocation(Location l) {
        if(locations == null) {
            locations = new HashSet<>();
        }
        locations.add(l);        
    }
    
    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        LocationCategory other = (LocationCategory) obj;
        boolean idsEqual = compareWithNulls(id, other.id);
        boolean namesEqual = compareWithNulls(name, other.name);
        return idsEqual && namesEqual;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    private boolean compareWithNulls(Object o1, Object o2) {
        if(o1 != null && o2 != null) return o1.equals(o2);
        if(o1 == null && o2 != null || o1 != null && o2 == null) return false;
        return true;
    }

}
